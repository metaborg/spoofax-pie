package mb.statix.multilang;

import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

@Value.Immutable
public interface FileResult extends Serializable {

    IStrategoTerm analyzedAst();

    SolverResult result();
}
