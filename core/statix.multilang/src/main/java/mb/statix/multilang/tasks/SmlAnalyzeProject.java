package mb.statix.multilang.tasks;

import com.google.common.collect.ListMultimap;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.constraints.CConj;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.ImmutableAnalysisResults;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.LoggerDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmlAnalyzeProject implements TaskDef<SmlAnalyzeProject.Input, SmlAnalyzeProject.Output> {
    private static final ILogger logger = LoggerUtils.logger(SmlAnalyzeProject.class);

    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final AnalysisContext analysisContext;

        public Input(ResourcePath projectPath, AnalysisContext analysisContext) {
            this.projectPath = projectPath;
            this.analysisContext = analysisContext;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectPath.equals(input.projectPath) &&
                analysisContext.equals(input.analysisContext);
        }

        @Override public int hashCode() {
            return Objects.hash(projectPath, analysisContext);
        }

        @Override public String toString() {
            return "Input{" +
                "projectPath=" + projectPath +
                ", analysisContext=" + analysisContext +
                '}';
        }
    }

    public static class Output implements Serializable {
        private final AnalysisResults results;

        public Output(AnalysisResults results) {
            this.results = results;
        }

        public AnalysisResults getResults() {
            return results;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Output output = (Output)o;
            return results.equals(output.results);
        }

        @Override
        public int hashCode() {
            return Objects.hash(results);
        }

        @Override public String toString() {
            return "Output{" +
                "results=" + results +
                '}';
        }
    }

    private final SmlInstantiateGlobalScope instantiateGlobalScope;
    private final SmlPartialSolveProject partialSolveProject;
    private final SmlPartialSolveFile partialSolveFile;

    @Inject public SmlAnalyzeProject(
        SmlInstantiateGlobalScope instantiateGlobalScope,
        SmlPartialSolveProject partialSolveProject,
        SmlPartialSolveFile partialSolveFile
    ) {
        this.instantiateGlobalScope = instantiateGlobalScope;
        this.partialSolveProject = partialSolveProject;
        this.partialSolveFile = partialSolveFile;
    }

    @Override public String getId() {
        return SmlAnalyzeProject.class.getSimpleName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        AnalysisContext analysisContext = input.analysisContext;
        Spec combinedSpec = analysisContext
            .languages()
            .values()
            .stream()
            .map(LanguageMetadata::statixSpec)
            .reduce(SpecBuilder::merge)
            .orElseThrow(() -> new MultiLangAnalysisException("Doing analysis without specs is not allowed"))
            .toSpec();

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

        @Nullable Level logLevel = input.analysisContext.stxLogLevel();
        final IDebugContext debug = logLevel != null ?
            new LoggerDebugContext(input.analysisContext.stxLogger(), logLevel) : new NullDebugContext();

        SmlInstantiateGlobalScope.Output globalState = context.require(instantiateGlobalScope.createTask(
            new SmlInstantiateGlobalScope.Input(input.analysisContext.contextId().toString(), debug, combinedSpec)));

        Map<LanguageId, SolverResult> projectResults = analysisContext.languages().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(
                    globalState.getGlobalScopeVar(),
                    globalState.getResult(),
                    debug,
                    combinedSpec,
                    entry.getValue().projectConstraint()))).getProjectResult()
            ));

        // Create file results (maintain resource key for error message mapping
        Map<AnalysisResults.FileKey, FileResult> fileResults = analysisContext.languages().values().stream()
            .flatMap(languageMetadata ->
                languageMetadata.resourcesSupplier().apply(context, input.projectPath).stream()
                    .map(resourceKey -> {
                        FileResult fileResult = context.require(partialSolveFile.createTask(new SmlPartialSolveFile.Input(globalState.getGlobalScope(),
                            globalState.getResult(), debug, combinedSpec, languageMetadata.fileConstraint(),
                            languageMetadata.astFunction(), languageMetadata.postTransform(),
                            resourceKey))).getFileResult();
                        return new AbstractMap.SimpleEntry<>(
                            new AnalysisResults.FileKey(languageMetadata.languageId(), resourceKey),
                            fileResult);
                    })
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        IState.Immutable combinedState = Stream.concat(
            projectResults.values().stream(),
            fileResults.values().stream().map(FileResult::getResult))
            .map(SolverResult::state)
            .reduce(IState.Immutable::add)
            .orElseThrow(() -> new MultiLangAnalysisException("Expected at least one result"));

        IConstraint combinedConstraint = Stream.concat(
            projectResults.values().stream(),
            fileResults.values().stream().map(FileResult::getResult))
            .map(SolverResult::delayed)
            .reduce(globalState.getResult().delayed(), CConj::new);

        long t0 = System.currentTimeMillis();
        SolverResult finalResult = Solver.solve(combinedSpec, combinedState, combinedConstraint, (s, l, st) -> true, debug);
        long dt = System.currentTimeMillis() - t0;
        logger.info("{} analyzed in {} ms] ", input.analysisContext.contextId(), dt);

        return new Output(ImmutableAnalysisResults.of(globalState.getGlobalScope(), new HashMap<>(projectResults), new HashMap<>(fileResults), finalResult));
    }
}
