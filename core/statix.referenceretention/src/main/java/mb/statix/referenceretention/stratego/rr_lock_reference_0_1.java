package mb.statix.referenceretention.stratego;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import static org.strategoxt.lang.Term.NO_STRATEGIES;
import static org.strategoxt.lang.Term.NO_TERMS;

/**
 * Invokes the {@link RRLockReferenceStrategy} primitive.
 * <p>
 * Use this as {@code <rr-lock-reference(|scope)> reference}.
 */
public final class rr_lock_reference_0_1 extends Strategy {
    /** The Strategy instance. */
    public static rr_lock_reference_0_1 instance = new rr_lock_reference_0_1();

    @Override public @Nullable IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm scope) {
        return context.invokePrimitive(RRLockReferenceStrategy.NAME, current, NO_STRATEGIES, new IStrategoTerm[] { scope });
    }
}
