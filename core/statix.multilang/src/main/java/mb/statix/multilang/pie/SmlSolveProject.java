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
import mb.statix.multilang.metadata.FileResult;
import mb.statix.multilang.metadata.ImmutableFileResult;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadata;
import mb.statix.multilang.metadata.LanguageMetadataManager;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
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

        @Override public boolean equals(@Nullable Object o) {
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
        this.logger = loggerFactory.create(SmlSolveProject.class);
    }

    @Override public String getId() {
        return SmlSolveProject.class.getCanonicalName();
    }

    @Override public Result<AnalysisResults, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        return input.languages.stream()
            .map(languageId -> context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(languageId,input.logLevel)))
                .map(res -> pair(languageId, res)))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("At least one project constraint has an unexpected exception", false)))
            .map(SmlSolveProject::entrySetToMap)
            .flatMap(projectResults -> analyzeFiles(context, input, projectResults));
    }

    private Result<AnalysisResults, MultiLangAnalysisException> analyzeFiles(
        ExecContext context,
        Input input,
        Map<LanguageId, SolverResult> projectResults
    ) {
        return getLanguageMetadata(input.languages).flatMap(lmds -> lmds.entrySet().stream()
            .flatMap(lmd -> lmd.getValue().resourcesSupplier().apply(context, input.projectPath).stream()
                .map(resourceKey -> context.require(fileResultSupplier(lmd.getKey(), resourceKey, input.logLevel))
                    .map(res -> pair((FileKey)ImmutableFileKey.builder()
                        .languageId(lmd.getKey())
                        .resourceKey(resourceKey)
                        .build(), res))))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("At least one file constraint has an unexpected exception", false)))
            .map(SmlSolveProject::entrySetToMap)
            .flatMap(fileResults -> context.require(buildSpec.createSupplier(new SmlBuildSpec.Input(input.languages)))
                // Upcast to make typing work
                .mapErr(MultiLangAnalysisException.class::cast)
                .flatMap(combinedSpec -> solveCombined(context, input, combinedSpec, projectResults, fileResults))));
    }

    private Result<AnalysisResults, MultiLangAnalysisException> solveCombined(
        ExecContext context,
        Input input,
        Spec combinedSpec,
        Map<LanguageId, SolverResult> projectResults,
        Map<FileKey, FileResult> fileResults
    ) {
         // Combine state of all intermediate results
         IState.Immutable combinedState = Stream.concat(
             projectResults.values().stream(),
             fileResults.values().stream().map(FileResult::result))
             .map(SolverResult::state)
             // When https://github.com/metaborg/nabl/commit/da4f60ca33cbd6566a0a4d42d00d39e9307e8d9d has landed in Spoofax 3
             // The identity State.of(combinedSpec) may be removed
             .reduce(State.of(combinedSpec), IState.Immutable::add);

         // Solve all residual constraints
         return context.require(instantiateGlobalScope.createTask(input.logLevel))
            // Solve residual constraints
            .flatMap(globalResult -> solveWithSpec(projectResults, fileResults, combinedState, globalResult.result().delayed(), combinedSpec, input.logLevel)
                // Apply post transformation on all files
                .flatMap(finalResult -> postTransform(context, input, fileResults, finalResult)
                    .map(newFileResults -> (AnalysisResults)ImmutableAnalysisResults.of(globalResult.globalScope(),
                        new HashMap<>(projectResults), newFileResults, finalResult))
                    .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                ));
    }

    private Result<SolverResult, MultiLangAnalysisException> solveWithSpec(
        Map<LanguageId, SolverResult> projectResults,
        Map<FileKey, FileResult> fileResults,
        IState.Immutable state,
        IConstraint globalConstraint,
        Spec combinedSpec,
        @Nullable Level logLevel
    ) {
        IDebugContext debug = SolverUtils.createDebugContext(logLevel);
        IConstraint combinedConstraint = Stream.concat(
            projectResults.values().stream(),
            fileResults.values().stream().map(FileResult::result))
            .map(SolverResult::delayed)
            .reduce(globalConstraint, CConj::new);

        try {
            long t0 = System.currentTimeMillis();
            SolverResult result = Solver.solve(combinedSpec, state, combinedConstraint, (s, l, st) -> true, debug, new NullProgress(), new NullCancel());
            long dt = System.currentTimeMillis() - t0;
            logger.info("Project analyzed in {} ms", dt);

            // Mark Delays as Errors
            final ImmutableMap.Builder<IConstraint, IMessage> messages = ImmutableMap.builder();
            messages.putAll(result.messages());
            result.delays().keySet().forEach(c -> messages.put(c, MessageUtil.findClosestMessage(c)));
            final SolverResult newResult = result.withMessages(messages.build()).withDelays(ImmutableMap.of());

            return Result.ofOk(newResult);
        } catch(InterruptedException e) {
           return Result.ofErr(new MultiLangAnalysisException(e));
        }
    }

    private Result<HashMap<FileKey, FileResult>, ?> postTransform(ExecContext context, Input input, Map<FileKey, FileResult> fileResults, SolverResult finalResult) {
        return fileResults.entrySet().stream()
            .map(entry -> languageMetadataManager.get().getLanguageMetadataResult(entry.getKey().languageId())
                .flatMap(lmd -> {
                    // Create supplier that partial file result with final solver result
                    Supplier<Result<FileResult, ?>> resultWithFinalResultSupplier = fileResultSupplier(entry.getKey().languageId(), entry.getKey().resourceKey(), input.logLevel)
                        .map(rs -> rs.map(fileResult -> ImmutableFileResult.builder().from(fileResult).result(finalResult).build()));
                    // Apply post transformation
                    return lmd.postTransform().apply(context, resultWithFinalResultSupplier)
                        // Build file result with transformed AST and original result
                        .map(ast -> ImmutableFileResult.builder().ast(ast).result(entry.getValue().result()).build())
                        .map(newResult -> pair(entry.getKey(), newResult))
                        .mapErr(MultiLangAnalysisException::wrapIfNeeded);
                }))
                .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Error applying post-transformations")))
                .map(SmlSolveProject::entrySetToMap)
                .map(HashMap::new);
    }

    private Result<Map<LanguageId, LanguageMetadata>, MultiLangAnalysisException> getLanguageMetadata(Collection<LanguageId> languages) {
        return languages.stream()
            .map(languageId -> languageMetadataManager.get().getLanguageMetadataResult(languageId)
                .map(res -> pair(languageId, res)))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Error when resolving language metadata", false)))
            .map(SmlSolveProject::entrySetToMap);
    }

    private Supplier<? extends Result<FileResult, ?>> fileResultSupplier(
        LanguageId languageId,
        mb.resource.ResourceKey resourceKey,
        @Nullable Level logLevel
    ) {
        return partialSolveFile.createSupplier(
            new SmlPartialSolveFile.Input(
                languageId,
                resourceKey,
                logLevel)
        );
    }

    private static <K, V> Map<K, V> entrySetToMap(Set<? extends Map.Entry<K, V>> entries) {
        return entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static <K, V>  Map.Entry<K,V> pair(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}
