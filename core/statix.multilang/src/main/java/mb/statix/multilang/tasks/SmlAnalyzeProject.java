package mb.statix.multilang.tasks;

import mb.common.util.UncheckedException;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.constraints.CConj;
import mb.statix.multilang.AAnalysisResults;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmlAnalyzeProject implements TaskDef<SmlAnalyzeProject.Input, SmlAnalyzeProject.Output> {

    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final AnalysisContext analysisContext;

        public Input(ResourcePath projectPath, AnalysisContext analysisContext) {
            this.projectPath = Objects.requireNonNull(projectPath, "SmlAnalyzeProject.Input.projectPath may not be null");
            this.analysisContext = Objects.requireNonNull(analysisContext, "SmlAnalyzeProject.Input.analysisContext may not be null");;
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
            .map(SpecBuilder::toSpec)
            .reduce(SpecUtils::mergeSpecs)
            .orElseThrow(() -> new RuntimeException("Doing analysis without specs is not allowed"));

        IDebugContext debug = new NullDebugContext();

        SmlInstantiateGlobalScope.Output globalState = context.require(instantiateGlobalScope.createTask(
            new SmlInstantiateGlobalScope.Input(input.analysisContext.contextId(), debug, combinedSpec)));

        Map<LanguageId, SolverResult> projectResults = analysisContext.languages().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                try {
                    return context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(
                        globalState.getGlobalScopeVar(),
                        globalState.getResult(),
                        debug,
                        combinedSpec,
                        entry.getValue().projectConstraint()))).getProjectResult();
                } catch(ExecException | InterruptedException e) {
                    throw new UncheckedException(e);
                }
            }));

        // Create file results (maintain resource key for error message mapping
        Map<AAnalysisResults.FileKey, FileResult> fileResults = analysisContext.languages().values().stream()
            .flatMap(languageMetadata -> {
                try {
                    return languageMetadata.resourcesSupplier().apply(context, input.projectPath).stream()
                        .map(resourceKey -> {
                            try {
                                FileResult fileResult = context.require(partialSolveFile.createTask(new SmlPartialSolveFile.Input(globalState.getGlobalScope(),
                                    globalState.getResult(), debug, combinedSpec, languageMetadata.fileConstraint(),
                                    languageMetadata.astFunction(), languageMetadata.postTransform(),
                                    resourceKey))).getFileResult();
                                return new AbstractMap.SimpleEntry<>(
                                    new AAnalysisResults.FileKey(languageMetadata.languageId(), resourceKey),
                                    fileResult);
                            } catch(ExecException | InterruptedException e) {
                                throw new UncheckedException(e);
                            }
                        });
                } catch(ExecException | InterruptedException e) {
                    throw new UncheckedException(e);
                }
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        IState.Immutable combinedState = Stream.concat(
                projectResults.values().stream(),
                fileResults.values().stream().map(FileResult::getResult))
            .map(SolverResult::state)
            .reduce(IState.Immutable::add)
            .orElseThrow(() -> new RuntimeException("Expected at least one result"));

        IConstraint combinedConstraint = Stream.concat(
                projectResults.values().stream(),
                fileResults.values().stream().map(FileResult::getResult))
            .map(SolverResult::delayed)
            .reduce(globalState.getResult().delayed(), CConj::new);

        SolverResult finalResult = Solver.solve(combinedSpec, combinedState, combinedConstraint, (s, l, st) -> true, debug);

        return new Output(AnalysisResults.of(globalState.getGlobalScope(), new HashMap<>(projectResults), new HashMap<>(fileResults), finalResult));
    }
}
