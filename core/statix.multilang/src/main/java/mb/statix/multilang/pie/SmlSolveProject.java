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
import mb.statix.multilang.pie.spec.SmlBuildSpec;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import static mb.statix.multilang.metadata.spec.SpecUtils.pair;
import static mb.statix.multilang.metadata.spec.SpecUtils.toMap;

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
        // Solve project constraints
        HashMap<LanguageId, Result<SolverResult, MultiLangAnalysisException>> projectResults = input.languages.stream()
            .map(languageId -> pair(languageId, context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(languageId, input.logLevel)))))
            .collect(toMap(HashMap::new));

        // Solve file constraints
        return analyzeFiles(context, input).map(fileResults -> {
            // Collect results of all successful runs
            HashSet<SolverResult> initialResults = new HashSet<>();
            projectResults.values().forEach(r -> r.ifOk(initialResults::add));
            fileResults.values().forEach(r -> r.map(FileResult::result).ifOk(initialResults::add));

            Result<SolverResult, MultiLangAnalysisException> finalResult = solveCombined(context, input, initialResults);

            HashMap<FileKey, Result<FileResult, MultiLangAnalysisException>> transFormedFileResults = finalResult
                .flatMap(result -> postTransform(context, input, fileResults, result))
                .getOr(fileResults);

            return ImmutableAnalysisResults.of(projectResults, transFormedFileResults, finalResult);
        });
    }

    private Result<HashMap<FileKey, Result<FileResult, MultiLangAnalysisException>>, MultiLangAnalysisException> analyzeFiles(
        ExecContext context,
        Input input
    ) {
        return getLanguageMetadata(input.languages)
            .map(lmds -> lmds.entrySet().stream()
                .flatMap(lmd -> lmd.getValue().resourcesSupplier().apply(context, input.projectPath).stream()
                    .map(resourceKey -> {
                        final FileKey fk = ImmutableFileKey.builder()
                            .languageId(lmd.getKey())
                            .resourceKey(resourceKey)
                            .build();
                        final Result<FileResult, MultiLangAnalysisException> res = context
                            .require(fileResultSupplier(lmd.getKey(), resourceKey, input.logLevel))
                            .mapErr(MultiLangAnalysisException::wrapIfNeeded);
                        return pair(fk, res);
                    }))
                .collect(toMap(HashMap::new)));
    }

    private Result<SolverResult, MultiLangAnalysisException> solveCombined(ExecContext context, Input input, HashSet<SolverResult> initialResults) {
        return context.require(buildSpec.createTask(new SmlBuildSpec.Input(input.languages)))
            // Upcast to make typing work
            .mapErr(MultiLangAnalysisException.class::cast)
            .flatMap(combinedSpec -> context.require(instantiateGlobalScope.createTask(input.logLevel)).flatMap(globalResult -> {
                // Combine state of all intermediate results
                final IState.Immutable combinedState = initialResults.stream()
                    .map(SolverResult::state)
                    // When https://github.com/metaborg/nabl/commit/da4f60ca33cbd6566a0a4d42d00d39e9307e8d9d has landed in Spoofax 3
                    // The identity State.of(combinedSpec) may be removed
                    .reduce(State.of(combinedSpec), IState.Immutable::add);
                final IConstraint combinedConstraint = initialResults.stream()
                    .map(SolverResult::delayed)
                    .reduce(globalResult.result().delayed(), CConj::new);

                try {
                    long t0 = System.currentTimeMillis();
                    IDebugContext debug = SolverUtils.createDebugContext(input.logLevel);
                    SolverResult result = Solver.solve(combinedSpec, combinedState, combinedConstraint, (s, l, st) -> true, debug, new NullProgress(), new NullCancel());
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
            }));
    }

    private Result<HashMap<FileKey, Result<FileResult, MultiLangAnalysisException>>, MultiLangAnalysisException> postTransform(
        ExecContext context,
        Input input, Map<FileKey, Result<FileResult, MultiLangAnalysisException>> fileResults,
        SolverResult finalResult
    ) {
        return fileResults.entrySet().stream()
            .map(entry -> {
                FileKey key = entry.getKey();
                return entry.getValue()
                    .flatMap(fileResult -> languageMetadataManager.get().getLanguageMetadataResult(key.languageId())
                        .map(lmd -> {
                            // Create supplier that partial file result with final solver result
                            Supplier<Result<FileResult, ?>> resultWithFinalResultSupplier = fileResultSupplier(key.languageId(), key.resourceKey(), input.logLevel)
                                .map(rs -> rs.map(fr -> ImmutableFileResult.builder().from(fr).result(finalResult).build()));
                            // Apply post transformation
                            Result<FileResult, MultiLangAnalysisException> transformResult = lmd.postTransform().apply(context, resultWithFinalResultSupplier)
                                // Build file result with transformed AST and original result
                                .map(ast -> (FileResult)ImmutableFileResult.builder().ast(ast).result(fileResult.result()).build())
                                .map(newResult -> newResult)
                                .mapErr(MultiLangAnalysisException::wrapIfNeeded);
                            return pair(key, transformResult);
                        }));
            })
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Error applying post-transformations")))
            .map(entries -> entries.stream().collect(toMap(HashMap::new)));
    }

    private Result<Map<LanguageId, LanguageMetadata>, MultiLangAnalysisException> getLanguageMetadata(Collection<LanguageId> languages) {
        return languages.stream()
            .map(languageId -> languageMetadataManager.get().getLanguageMetadataResult(languageId)
                .map(res -> pair(languageId, res)))
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Error when resolving language metadata", false)))
            .map(entries -> entries.stream().collect(toMap(HashMap::new)));
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
}
