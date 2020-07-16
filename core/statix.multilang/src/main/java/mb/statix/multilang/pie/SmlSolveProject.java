package mb.statix.multilang.pie;

import com.google.common.collect.ImmutableMap;
import dagger.Lazy;
import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.constraints.CConj;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageUtil;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.FileKey;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.ImmutableAnalysisResults;
import mb.statix.multilang.ImmutableFileKey;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.LanguageMetadataManager;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MultiLangScope
public class SmlSolveProject implements TaskDef<SmlSolveProject.Input, Result<AnalysisResults, MultiLangAnalysisException>> {
    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final HashSet<LanguageId> languages;
        private final @Nullable Level logLevel;

        public Input(ResourcePath projectPath, HashSet<LanguageId> languages, @Nullable Level logLevel) {
            this.projectPath = projectPath;
            this.languages = languages;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectPath.equals(input.projectPath) &&
                languages.equals(input.languages);
        }

        @Override public int hashCode() {
            return Objects.hash(projectPath, languages);
        }

        @Override public String toString() {
            return "Input{" +
                "projectPath=" + projectPath +
                ", languages=" + languages +
                '}';
        }
    }

    private final SmlInstantiateGlobalScope instantiateGlobalScope;
    private final SmlPartialSolveProject partialSolveProject;
    private final SmlPartialSolveFile partialSolveFile;
    private final SmlBuildSpec buildSpec;
    private final Lazy<LanguageMetadataManager> languageMetadataManager;
    private final Logger logger;

    @Inject public SmlSolveProject(
        SmlInstantiateGlobalScope instantiateGlobalScope,
        SmlPartialSolveProject partialSolveProject,
        SmlPartialSolveFile partialSolveFile,
        SmlBuildSpec buildSpec,
        @MultiLang Lazy<LanguageMetadataManager> languageMetadataManager,
        LoggerFactory loggerFactory
    ) {
        this.instantiateGlobalScope = instantiateGlobalScope;
        this.partialSolveProject = partialSolveProject;
        this.partialSolveFile = partialSolveFile;
        this.buildSpec = buildSpec;
        this.languageMetadataManager = languageMetadataManager;
        logger = loggerFactory.create(SmlSolveProject.class);
    }

    @Override public String getId() {
        return SmlSolveProject.class.getCanonicalName();
    }

    @Override public Result<AnalysisResults, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        return getLanguageMetadata(input.languages).flatMap(languageMetadataMap -> languageMetadataMap.values().stream()
            .map(lmd -> context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(
                globalResultSupplier(input),
                specSupplier(input),
                lmd.projectConstraint(),
                input.logLevel))).map(res -> new AbstractMap.SimpleImmutableEntry<>(lmd.languageId(), res)))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("At least one project constraint has an unexpected exception", false)))
            .map(SmlSolveProject::entrySetToMap)
            .flatMap(projectResults -> analyzeFiles(context, input, languageMetadataMap, projectResults)));
    }

    private Result<AnalysisResults, MultiLangAnalysisException> analyzeFiles(
        ExecContext context,
        Input input,
        Map<LanguageId, LanguageMetadata> languageMetadataMap,
        Map<LanguageId, SolverResult> projectResults
    ) {
        return languageMetadataMap.values().stream()
            .flatMap(languageMetadata -> languageMetadata.resourcesSupplier().apply(context, input.projectPath).stream()
                .map(resourceKey -> context.require(partialSolveFile.createTask(new SmlPartialSolveFile.Input(
                    languageMetadata.languageId(),
                    resourceKey,
                    specSupplier(input),
                    globalResultSupplier(input),
                    input.logLevel)))
                    .map(res -> new AbstractMap.SimpleImmutableEntry<FileKey, FileResult>(ImmutableFileKey.builder()
                        .languageId(languageMetadata.languageId())
                        .resourceKey(resourceKey)
                        .build(), res))))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("At least one file constraint has an unexpected exception", false)))
            .map(SmlSolveProject::entrySetToMap)
            .flatMap(fileResults -> solveCombined(context, input, projectResults, fileResults));
    }

    private Result<AnalysisResults, MultiLangAnalysisException> solveCombined(
        ExecContext context,
        Input input,
        Map<LanguageId, SolverResult> projectResults,
        Map<FileKey, FileResult> fileResults
    ) {
        return Stream.concat(
            projectResults.values().stream(),
            fileResults.values().stream().map(FileResult::result))
            .map(SolverResult::state)
            .reduce(IState.Immutable::add)
            .<Result<AnalysisResults, MultiLangAnalysisException>>map(state ->
                TaskUtils.executeWrapped(() -> context.require(globalResultSupplier(input))
                        .flatMap(globalResult -> TaskUtils.executeWrapped(() -> context.require(specSupplier(input))
                            // Upcast to make typing work
                            .mapErr(MultiLangAnalysisException.class::cast)
                            .flatMap(combinedSpec -> solveWithSpec(projectResults, fileResults, state, globalResult, combinedSpec, input.logLevel)), "Solving final constraints interrupted")
                            .map(finalResult -> ImmutableAnalysisResults.of(globalResult.globalScope(),
                                new HashMap<>(projectResults), new HashMap<>(fileResults), finalResult))),
                    "IO exception while requiring global result"))
            .orElseGet(() -> Result.ofErr(new MultiLangAnalysisException("BUG: Analysis gave no results")));
    }

    private Result<SolverResult, MultiLangAnalysisException> solveWithSpec(
        Map<LanguageId, SolverResult> projectResults,
        Map<FileKey, FileResult> fileResults,
        IState.Immutable state,
        GlobalResult globalResult,
        Spec combinedSpec,
        @Nullable Level logLevel
    ) {
        return TaskUtils.executeWrapped(() -> {
            IDebugContext debug = TaskUtils.createDebugContext(logLevel);
            IConstraint combinedConstraint = Stream.concat(
                projectResults.values().stream(),
                fileResults.values().stream().map(FileResult::result))
                .map(SolverResult::delayed)
                .reduce(globalResult.result().delayed(), CConj::new);
            long t0 = System.currentTimeMillis();
            SolverResult result = Solver.solve(combinedSpec, state, combinedConstraint, (s, l, st) -> true, debug, new NullProgress(), new NullCancel());
            long dt = System.currentTimeMillis() - t0;
            logger.info("Project analyzed in {} ms", dt);

            // Mark Delays as Errors
            final ImmutableMap.Builder<IConstraint, IMessage> messages = ImmutableMap.builder();
            messages.putAll(result.messages());
            result.delays().keySet().forEach(c -> {
                messages.put(c, MessageUtil.findClosestMessage(c));
            });
            final SolverResult newResult = result.withMessages(messages.build()).withDelays(ImmutableMap.of());

            return Result.ofOk(newResult);
        });
    }

    private Result<Map<LanguageId, LanguageMetadata>, MultiLangAnalysisException> getLanguageMetadata(Collection<LanguageId> languages) {
        return languages.stream()
            .map(languageId -> TaskUtils.executeWrapped(() -> Result.ofOk(languageMetadataManager.get().getLanguageMetadata(languageId)))
                .map(res -> new AbstractMap.SimpleImmutableEntry<>(languageId, res)))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Error when resolving language metadata", false)))
            .map(SmlSolveProject::entrySetToMap);
    }

    private Supplier<Result<Spec, SpecLoadException>> specSupplier(Input input) {
        return buildSpec.createSupplier(new SmlBuildSpec.Input(input.languages));
    }

    private Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier(Input input) {
        return instantiateGlobalScope.createSupplier(new SmlInstantiateGlobalScope.Input(input.logLevel, specSupplier(input)));
    }

    private static <K, V> Map<K, V> entrySetToMap(Set<? extends Map.Entry<K, V>> entries) {
        return entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
