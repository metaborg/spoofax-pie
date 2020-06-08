package mb.statix.multilang.tasks;

import mb.common.util.UncheckedException;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CConj;
import mb.statix.multilang.AAnalysisResults;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.utils.SpecUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmlAnalyzeProject implements TaskDef<SmlAnalyzeProject.Input, SmlAnalyzeProject.Output> {

    public static class Input implements Serializable {
        private final AnalysisContext analysisContext;

        public Input(AnalysisContext analysisContext) {
            this.analysisContext = analysisContext;
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

    private final SmlInstantiateGlobalScope instantiateGlobalScope = new SmlInstantiateGlobalScope();
    private final SmlPartialSolveProject partialSolveProject = new SmlPartialSolveProject();
    private final SmlPartialSolveFile partialSolveFile = new SmlPartialSolveFile();

    @Override public String getId() {
        return SmlAnalyzeProject.class.getSimpleName();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        AnalysisContext analysisContext = input.analysisContext;
        Spec combinedSpec = analysisContext.languages().values().stream()
            .map(LanguageMetadata::statixSpec)
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
        Map<AAnalysisResults.FileKey, SolverResult> fileResults = analysisContext.languages().values().stream()
            .flatMap(languageMetadata -> {
                try {
                    return languageMetadata.resourcesSupplier().get(context).stream()
                        .map(resourceKey -> {
                            try {
                                SolverResult fileResult = context.require(partialSolveFile.createTask(new SmlPartialSolveFile.Input(globalState.getGlobalScope(),
                                    globalState.getResult(), debug, combinedSpec, languageMetadata.fileConstraint(),
                                    languageMetadata.astFunction(),
                                    resourceKey))).getFileResult();
                                return new AbstractMap.SimpleEntry<>(
                                    new AAnalysisResults.FileKey(languageMetadata.languageId(), resourceKey),
                                    fileResult);
                            } catch(ExecException | InterruptedException e) {
                                throw new UncheckedException(e);
                            }
                        });
                } catch(ExecException | IOException | InterruptedException e) {
                    throw new UncheckedException(e);
                }
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        IState.Immutable combinedState = Stream.concat(projectResults.values().stream(), fileResults.values().stream())
            .map(SolverResult::state)
            .reduce(IState.Immutable::add)
            .orElseThrow(() -> new RuntimeException("Expected at least one result"));

        IConstraint combinedConstraint = Stream.concat(projectResults.values().stream(), fileResults.values().stream())
            .map(SolverResult::delayed)
            .reduce(globalState.getResult().delayed(), CConj::new);

        SolverResult finalResult = Solver.solve(combinedSpec, combinedState, combinedConstraint, (s, l, st) -> true, debug);

        return new Output(AnalysisResults.of(globalState.getGlobalScope(), new HashMap<>(projectResults), new HashMap<>(fileResults), finalResult));
    }
}
