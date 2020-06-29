package mb.statix.multilang;

import mb.statix.solver.persistent.SolverResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public class FileResult implements Serializable {

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

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        FileResult that = (FileResult)o;
        return analyzedAst.equals(that.analyzedAst) &&
            result.equals(that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analyzedAst, result);
    }

    @Override public String toString() {
        return "FileResult{" +
            "analyzedAst=" + analyzedAst.toString(2) +
            ", result=" + result.getClass().getName() +
            '}';
    }
}
