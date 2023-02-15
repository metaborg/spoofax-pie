package mb.statix.referenceretention.stratego;

import mb.nabl2.terms.ITerm;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import static org.strategoxt.lang.Term.NO_STRATEGIES;

/**
 * Invokes the {@link RRLockReferenceStrategy} primitive.
 */
public final class rr_lock_reference_0_3 extends Strategy {
    /** The Strategy instance. */
    public static rr_lock_reference_0_3 instance = new rr_lock_reference_0_3();

    @Override public @Nullable IStrategoTerm invoke(
        Context context,
        IStrategoTerm current,
        IStrategoTerm decl,
        IStrategoTerm solverResultTerm,
        IStrategoTerm sortName
    ) {
        // NOTE: Ensure the strategy has the same number of tvars as the primitive it's wrapping.
        assert(RRLockReferenceStrategy.SVARS == 0);
        assert(RRLockReferenceStrategy.TVARS == 3);
        return context.invokePrimitive(RRLockReferenceStrategy.NAME, current, NO_STRATEGIES, new IStrategoTerm[] {
            decl,
            solverResultTerm,
            sortName
        });
    }
}
