package mb.statix.multilang;

import mb.statix.solver.persistent.SolverResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class FileResult {

    private final IStrategoTerm analyzedAst;
    private final SolverResult result;

    public FileResult(IStrategoTerm analyzedAst, SolverResult result) {
        this.analyzedAst = analyzedAst;
        this.result = result;
    }

    public IStrategoTerm getAnalyzedAst() {
        return analyzedAst;
    }

    public SolverResult getResult() {
        return result;
    }
}
