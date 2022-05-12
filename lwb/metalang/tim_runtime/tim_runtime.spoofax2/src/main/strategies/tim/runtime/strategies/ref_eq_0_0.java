package tim.runtime.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class ref_eq_0_0 extends Strategy {
    public static ref_eq_0_0 instance = new ref_eq_0_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        if (!TermUtils.isTuple(current, 2)) {
            return null;
        }

        IStrategoTuple tuple = (IStrategoTuple) current;
        IStrategoTerm left = tuple.get(0);
        IStrategoTerm right = tuple.get(1);

        if (left == right) {
            return current;
        }

        return null;
    }
}
