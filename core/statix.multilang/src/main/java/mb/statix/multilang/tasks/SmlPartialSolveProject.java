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
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class SmlPartialSolveProject implements TaskDef<SmlPartialSolveProject.Input, SmlPartialSolveProject.Output> {

    public static class Input implements Serializable {
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

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return globalScopeVar.equals(input.globalScopeVar) &&
                globalResult.equals(input.globalResult) &&
                spec.equals(input.spec) &&
                projectConstraint.equals(input.projectConstraint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(globalScopeVar, globalResult, spec, projectConstraint);
        }

        @Override public String toString() {
            return "Input{" +
                "globalScopeVar=" + globalScopeVar +
                ", globalResult=" + globalResult +
                ", spec=" + spec +
                ", projectConstraint='" + projectConstraint + '\'' +
                '}';
        }
    }

    public static class Output implements Serializable {
        private final SolverResult projectResult;

        public Output(SolverResult projectResult) {
            this.projectResult = projectResult;
        }

        public SolverResult getProjectResult() {
            return projectResult;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Output output = (Output)o;
            return projectResult.equals(output.projectResult);
        }

        @Override public int hashCode() {
            return Objects.hash(projectResult);
        }

        @Override public String toString() {
            return "Output{" +
                "projectResult=" + projectResult +
                '}';
        }
    }

    @Inject public SmlPartialSolveProject() {
    }

    @Override
    public String getId() {
        return SmlPartialSolveProject.class.getSimpleName();
    }

    @Override
    public Output exec(ExecContext context, Input input) throws Exception {
        Iterable<ITermVar> scopeArgs = Iterables2.singleton(input.globalScopeVar);
        IConstraint projectConstraint = new CUser(input.projectConstraint, scopeArgs);

        SolverResult result = SolverUtils.partialSolve(input.spec, input.globalResult.state(), projectConstraint,
            input.debug, new NullProgress(), new NullCancel());

        return new Output(result);
    }
}
