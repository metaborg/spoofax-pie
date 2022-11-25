package mb.statix.referenceretention.stratego;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import static org.strategoxt.lang.Term.NO_STRATEGIES;

/**
 * Invokes the {@link RRCreatePlaceholderStrategy} primitive.
 * <p>
 * Use this as {@code <rr-create-placeholder(|ctx)> body}.
 */
public final class rr_create_placeholder_0_1 extends Strategy {
    /** The Strategy instance. */
    public static rr_create_placeholder_0_1 instance = new rr_create_placeholder_0_1();

    @Override public @Nullable IStrategoTerm invoke(Context context, IStrategoTerm current, IStrategoTerm ctx) {
        return context.invokePrimitive(RRCreatePlaceholderStrategy.NAME, current, NO_STRATEGIES, new IStrategoTerm[] { ctx });
    }
}
