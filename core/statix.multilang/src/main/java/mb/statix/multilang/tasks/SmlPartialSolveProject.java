package mb.statix.multilang.tasks;

import mb.nabl2.terms.ITermVar;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CUser;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.metaborg.util.iterators.Iterables2;

import java.io.Serializable;

public class SmlPartialSolveProject implements TaskDef<SmlPartialSolveProject.Input, SmlPartialSolveProject.Output> {

    public static class Input implements Serializable  {
        private final ITermVar globalScopeVar;
        private final SolverResult globalResult;
        private final IDebugContext debug;

        private final Spec spec;
        private final String projectConstraint;

        public Input(ITermVar globalScopeVar, SolverResult globalResult, IDebugContext debug, Spec spec, String projectConstraint) {
            this.globalScopeVar = globalScopeVar;
            this.globalResult = globalResult;
            this.debug = debug;
            this.spec = spec;
            this.projectConstraint = projectConstraint;
        }
    }

    public static class Output implements Serializable  {
        private final SolverResult projectResult;

        public Output(SolverResult projectResult) {
            this.projectResult = projectResult;
        }

        public SolverResult getProjectResult() {
            return projectResult;
        }
    }

    @Override
    public String getId() {
        return SmlPartialSolveProject.class.getSimpleName();
    }

    @Override
    public Output exec(ExecContext context, Input input) throws Exception {
        Iterable<ITermVar> scopeArgs = Iterables2.singleton(input.globalScopeVar);
        IConstraint projectConstraint = new CUser(input.projectConstraint, scopeArgs);

        SolverResult result = SolverUtils.partialSolve(input.spec, input.globalResult.state(), projectConstraint, input.debug);

        return new Output(result);
    }
}
