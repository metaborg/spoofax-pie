package mb.statix.multilang.pie;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Objects;

@Value.Immutable
public interface GlobalResult extends Serializable {
    ITerm globalScope();

    ITermVar globalScopeVar();

    SolverResult result();
}
