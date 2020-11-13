package mb.statix.multilang.metadata;

import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

@Value.Immutable
public interface FileResult extends Serializable {

    IStrategoTerm ast();

    SolverResult result();
}
