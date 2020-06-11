package mb.statix.multilang.tasks;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.ImmutableTermVar;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CExists;
import mb.statix.constraints.CNew;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import org.metaborg.util.iterators.Iterables2;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class SmlInstantiateGlobalScope implements TaskDef<SmlInstantiateGlobalScope.Input, SmlInstantiateGlobalScope.Output> {

    public static class Input implements Serializable {
        private final String globalScopeName;
        private final IDebugContext debug;
        private final Spec spec;

        public Input(String globalScopeName, IDebugContext debug, Spec spec) {
            this.globalScopeName = globalScopeName;
            this.debug = debug;
            this.spec = spec;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            // Debug context should not influence results, so consider inputs with different debug settings as equal.
            return globalScopeName.equals(input.globalScopeName) &&
                spec.equals(input.spec);
        }

        @Override public int hashCode() {
            return Objects.hash(globalScopeName, spec);
        }

        @Override public String toString() {
            return "Input{" +
                "globalScopeName='" + globalScopeName + '\'' +
                ", spec=" + spec +
                '}';
        }
    }

    public static class Output implements Serializable {
        private final ITerm globalScope;
        private final ITermVar globalScopeVar;
        private final SolverResult result;

        public Output(ITerm globalScope, ITermVar globalScopeVar, SolverResult result) {
            this.globalScope = globalScope;
            this.globalScopeVar = globalScopeVar;
            this.result = result;
        }

        public ITerm getGlobalScope() {
            return globalScope;
        }

        public ITermVar getGlobalScopeVar() {
            return globalScopeVar;
        }

        public SolverResult getResult() {
            return result;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Output output = (Output)o;
            return globalScope.equals(output.globalScope) &&
                globalScopeVar.equals(output.globalScopeVar) &&
                result.equals(output.result);
        }

        @Override public int hashCode() {
            return Objects.hash(globalScope, globalScopeVar, result);
        }

        @Override public String toString() {
            return "Output{" +
                "globalScope=" + globalScope +
                ", globalScopeVar=" + globalScopeVar +
                ", result=" + result +
                '}';
        }
    }

    @Inject public SmlInstantiateGlobalScope() { }

    @Override
    public String getId() {
        return SmlInstantiateGlobalScope.class.getSimpleName();
    }

    @Override
    public Output exec(ExecContext context, Input input) throws Exception {
        // Create uniquely named scope variable
        ITermVar globalScopeVar = ImmutableTermVar.of("", String.format("global-%s", input.globalScopeName));
        Iterable<ITermVar> scopeArgs = Iterables2.singleton(globalScopeVar);
        IConstraint globalConstraint = new CExists(scopeArgs, new CNew(Iterables2.fromConcat(scopeArgs)));
        IState.Immutable state = State.of(input.spec);

        SolverResult result = SolverUtils.partialSolve(input.spec, state, globalConstraint, input.debug);

        ITerm globalScope = result.state().unifier()
            .findRecursive(result.existentials().get(globalScopeVar));

        return new Output(globalScope, globalScopeVar, result);
    }
}
