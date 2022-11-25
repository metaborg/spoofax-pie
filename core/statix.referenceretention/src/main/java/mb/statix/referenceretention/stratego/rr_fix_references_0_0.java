package mb.statix.referenceretention.stratego;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.strategoxt.lang.Strategy;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;

import static org.strategoxt.lang.Term.NO_STRATEGIES;
import static org.strategoxt.lang.Term.NO_TERMS;

/**
 * Invokes the {@link RRFixReferencesStrategy} primitive.
 * <p>
 * Use this as {@code <rr-fix-references> body}.
 */
public final class rr_fix_references_0_0 extends Strategy {
    /** The Strategy instance. */
    public static rr_fix_references_0_0 instance = new rr_fix_references_0_0();

    @Override public @Nullable IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return context.invokePrimitive(RRFixReferencesStrategy.NAME, current, NO_STRATEGIES, NO_TERMS);
    }
}
