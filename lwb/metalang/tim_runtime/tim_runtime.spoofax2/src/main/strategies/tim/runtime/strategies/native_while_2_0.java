package tim.runtime.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class native_while_2_0 extends Strategy {
    public static native_while_2_0 instance = new native_while_2_0();

    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current, Strategy condition, Strategy body) {
        while (true) {
            IStrategoTerm next = condition.invoke(context, current);
            if (next == null) {
                return current;
            }

            IStrategoTerm after = body.invoke(context, next);
            if (after == null) {
                return next;
            }

            current = after;
        }
    }
}
