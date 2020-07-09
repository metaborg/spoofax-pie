package mb.statix.multilang.pie;

import com.google.common.collect.ListMultimap;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.constraints.CConj;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.FileAnalysisException;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.ImmutableAnalysisResults;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.ProjectAnalysisException;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MultiLangScope
public class SmlAnalyzeProject implements TaskDef<SmlAnalyzeProject.Input, Result<AnalysisResults, MultiLangAnalysisException>> {
    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final HashSet<LanguageId> languages;
        private final Level logLevel;

        public Input(ResourcePath projectPath, HashSet<LanguageId> languages, Level logLevel) {
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
    private final AnalysisContextService analysisContextService;
    private final Logger logger;

    @Inject public SmlAnalyzeProject(
        SmlInstantiateGlobalScope instantiateGlobalScope,
        SmlPartialSolveProject partialSolveProject,
        SmlPartialSolveFile partialSolveFile,
        SmlBuildSpec buildSpec,
        AnalysisContextService analysisContextService,
        LoggerFactory loggerFactory
    ) {
        this.instantiateGlobalScope = instantiateGlobalScope;
        this.partialSolveProject = partialSolveProject;
        this.partialSolveFile = partialSolveFile;
        this.buildSpec = buildSpec;
        this.analysisContextService = analysisContextService;
        logger = loggerFactory.create(SmlAnalyzeProject.class);
    }

    @Override public String getId() {
        return SmlAnalyzeProject.class.getSimpleName();
    }

    @Override public Result<AnalysisResults, MultiLangAnalysisException> exec(ExecContext context, Input input) throws Exception {
        final Supplier<Result<Spec, SpecLoadException>> specSupplier = buildSpec.createSupplier(new SmlBuildSpec.Input(input.languages));
        return context.require(specSupplier)
            .mapErr(MultiLangAnalysisException::new)
            .flatMap(combinedSpec -> {
                final ListMultimap<String, Rule> rulesWithEquivalentPatterns = combinedSpec.rules().getAllEquivalentRules();
                if(!rulesWithEquivalentPatterns.isEmpty()) {
                    logger.error("+--------------------------------------+");
                    logger.error("| FOUND RULES WITH EQUIVALENT PATTERNS |");
                    logger.error("+--------------------------------------+");
                    for(Map.Entry<String, Collection<Rule>> entry : rulesWithEquivalentPatterns.asMap().entrySet()) {
                        logger.error("| Overlapping rules for: {}", entry.getKey());
                        for(Rule rule : entry.getValue()) {
                            logger.error("| * {}", rule);
                        }
                    }
                    logger.error("+--------------------------------------+");
                }

                IDebugContext debug = TaskUtils.createDebugContext("MLA [%s]", input.logLevel);

                Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier = instantiateGlobalScope.createSupplier(
                    new SmlInstantiateGlobalScope.Input(input.logLevel, specSupplier));

                Map<LanguageId, Result<SolverResult, MultiLangAnalysisException>> projectResults = input.languages.stream()
                    .collect(Collectors.toMap(Function.identity(), languageId ->
                        context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(
                            globalResultSupplier,
                            specSupplier,
                            analysisContextService.getLanguageMetadata(languageId).projectConstraint(),
                            input.logLevel)))
                    ));

                // If project constraint solving failed, return error value
                if(projectResults.values().stream().anyMatch(Result::isErr)) {
                    MultiLangAnalysisException aggregateException = new MultiLangAnalysisException("At least one project constraint has an unexpected exception");
                    projectResults.forEach((key, value) -> value.ifErr(err -> aggregateException.addSuppressed(new ProjectAnalysisException(key, err))));
                    return Result.ofErr(aggregateException);
                }

                // Create file results (maintain resource key for error message mapping
                Map<ResourceKey, Result<FileResult, MultiLangAnalysisException>> fileResults = input.languages.stream()
                    .flatMap(languageId -> analysisContextService
                        .getLanguageMetadata(languageId)
                        .resourcesSupplier()
                        .apply(context, input.projectPath)
                        .stream()
                        .map(resourceKey -> new AbstractMap.SimpleEntry<>(
                            resourceKey,
                            context.require(partialSolveFile.createTask(new SmlPartialSolveFile.Input(
                                languageId,
                                resourceKey,
                                specSupplier,
                                globalResultSupplier,
                                input.logLevel))))))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                // If file constraint solving failed, return error value
                if(fileResults.values().stream().anyMatch(Result::isErr)) {
                    MultiLangAnalysisException aggregateException = new MultiLangAnalysisException("At least one file constraint has an unexpected exception");
                    fileResults.forEach((key, value) -> value.ifErr(err -> aggregateException.addSuppressed(new FileAnalysisException(key, err))));
                    return Result.ofErr(aggregateException);
                }

                Map<LanguageId, SolverResult> unWrappedProjectResults = projectResults
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().unwrap()));

                Map<ResourceKey, FileResult> unWrappedFileResults = fileResults
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().unwrap()));

                IState.Immutable combinedState = Stream.concat(
                    unWrappedProjectResults.values().stream(),
                    unWrappedFileResults.values().stream().map(FileResult::getResult))
                    .map(SolverResult::state)
                    .reduce(IState.Immutable::add)
                    .orElseThrow(() -> new MultiLangAnalysisException("Expected at least one result"));

                return TaskUtils.executeIOWrapped(() -> context.require(globalResultSupplier).flatMap(globalResult -> {
                    IConstraint combinedConstraint = Stream.concat(
                        unWrappedProjectResults.values().stream(),
                        unWrappedFileResults.values().stream().map(FileResult::getResult))
                        .map(SolverResult::delayed)
                        .reduce(globalResult.getResult().delayed(), CConj::new);

                    return TaskUtils.executeInterruptionWrapped(() -> {
                        long t0 = System.currentTimeMillis();
                        SolverResult result = Solver.solve(combinedSpec,
                            combinedState, combinedConstraint, (s, l, st) -> true, debug, new NullProgress(), new NullCancel());
                        long dt = System.currentTimeMillis() - t0;
                        logger.info("Project analyzed in {} ms", dt);
                        return Result.ofOk(result);
                    }, "Solving final constraints interrupted")
                    .map(finalResult -> ImmutableAnalysisResults.of(globalResult.getGlobalScope(),
                        new HashMap<>(unWrappedProjectResults), new HashMap<>(unWrappedFileResults), finalResult));
                }), "IO exception while requiring global result");
            });
    }
}
