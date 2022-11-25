package mb.statix.referenceretention.strategies.runtime;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

/**
 * Fixes the references in the AST.
 * <p>
 * In Stratego, declare this strategy as {@code external fix-references}.
 */
public final class fix_references_0_0 extends Strategy {

    /** The instance. */
    public static final fix_references_0_0 instance = new fix_references_0_0();

    @Override public @Nullable IStrategoTerm invoke(Context context, IStrategoTerm current) {
        // TODO: This should call the Tego engine and strategy.
        throw new IllegalStateException("Strategy fix_references_0_0 called!");
    }
}
