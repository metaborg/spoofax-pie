package mb.statix.common.context;

import mb.nabl2.terms.ITerm;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.SolverResult;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "A*")
public interface CachedAnalysis {
    @Nullable ITerm globalScope();
    @Nullable SolverResult globalState();
    @Nullable IConstraint globalConstraint();
}
