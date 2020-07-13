package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.TermVar;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CExists;
import mb.statix.constraints.CNew;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import static mb.common.result.Result.ofOk;

@MultiLangScope
public class SmlInstantiateGlobalScope implements TaskDef<SmlInstantiateGlobalScope.Input, Result<GlobalResult, MultiLangAnalysisException>> {

    public static class Input implements Serializable {
        private final @Nullable Level logLevel;
        private final Supplier<Result<Spec, SpecLoadException>> specSupplier;

        public Input(@Nullable Level logLevel, Supplier<Result<Spec, SpecLoadException>> specSupplier) {
            this.logLevel = logLevel;
            this.specSupplier = specSupplier;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            // Debug context should not influence results, so consider inputs with different debug settings as equal.
            return specSupplier.equals(input.specSupplier);
        }

        @Override public int hashCode() {
            return Objects.hash(specSupplier);
        }

        @Override public String toString() {
            return "Input{" +
                "specSupplier=" + specSupplier +
                '}';
        }
    }

    @Inject public SmlInstantiateGlobalScope() {
    }

    @Override
    public String getId() {
        return SmlInstantiateGlobalScope.class.getSimpleName();
    }

    @Override
    public Result<GlobalResult, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        // Create uniquely named scope variable
        try {
            return context.require(input.specSupplier)
                .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                .flatMap(spec -> instantiateGlobalScopeForSpec(input, spec));
        } catch(IOException e) {
            return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded("Error while creating global scope: cannot load specification", e));
        }
    }

    private Result<GlobalResult, MultiLangAnalysisException> instantiateGlobalScopeForSpec(Input input, Spec spec) {
        ITermVar globalScopeVar = TermVar.of("", "s");
        Iterable<ITermVar> scopeArgs = Iterables2.singleton(globalScopeVar);
        IConstraint globalConstraint = new CExists(scopeArgs, new CNew(Iterables2.fromConcat(scopeArgs)));
        IState.Immutable state = State.of(spec);
        IDebugContext debug = TaskUtils.createDebugContext("MLA", input.logLevel);

        try {
            SolverResult result = SolverUtils.partialSolve(spec, state, globalConstraint, debug,
                new NullProgress(), new NullCancel());
            ITerm globalScope = result.state().unifier()
                .findRecursive(result.existentials().get(globalScopeVar));
            return Result.ofOk(new GlobalResult(globalScope, globalScopeVar, result));
        } catch(InterruptedException e) {
            return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded("Constraint solving interrupted", e));
        }
    }
}
