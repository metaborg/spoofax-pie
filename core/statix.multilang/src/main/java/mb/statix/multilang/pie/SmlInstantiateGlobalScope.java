package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.TermVar;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CExists;
import mb.statix.constraints.CNew;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Set;

@MultiLangScope
public class SmlInstantiateGlobalScope implements TaskDef<SmlInstantiateGlobalScope.Input, Result<GlobalResult, MultiLangAnalysisException>> {

    public static class Input implements Serializable {
        private final @Nullable Level logLevel;

        public Input(@Nullable Level logLevel) {
            this.logLevel = logLevel;
        }

        @Override public boolean equals(@Nullable Object other) {
            // Debug context should not influence results, so consider inputs with different debug settings as equal.
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() {
            return 0;
        }

        @Override public String toString() {
            return "Input{}";
        }
    }

    @Inject public SmlInstantiateGlobalScope() {
    }

    @Override
    public String getId() {
        return SmlInstantiateGlobalScope.class.getCanonicalName();
    }

    @Override
    public Result<GlobalResult, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        try {
            ITermVar globalScopeVar = TermVar.of("<global-scope>", "s");
            Set<ITermVar> scopeArgs = Collections.singleton(globalScopeVar);
            IConstraint globalConstraint = new CExists(scopeArgs, new CNew(globalScopeVar, globalScopeVar));
            IState.Immutable state = State.of().withResource("<global-scope>");
            IDebugContext debug = SolverUtils.createDebugContext(input.logLevel);

            try {
                SolverResult result = SolverUtils.partialSolve(Spec.of(), state, globalConstraint, debug, new NullCancel(), new NullProgress());
                ITerm globalScope = result.state().unifier().findRecursive(result.existentials().get(globalScopeVar));
                return Result.ofOk(ImmutableGlobalResult.builder()
                    .globalScope(globalScope)
                    .result(result)
                    .build());
            } catch(InterruptedException e) {
                return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded("Constraint solving interrupted", e));
            }
        } catch(UncheckedIOException e) {
            return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded("Error while creating global scope: cannot load specification", e.getCause()));
        }
    }

    public Task<Result<GlobalResult, MultiLangAnalysisException>> createTask(@Nullable Level logLevel) {
        return createTask(new Input(logLevel));
    }
}
