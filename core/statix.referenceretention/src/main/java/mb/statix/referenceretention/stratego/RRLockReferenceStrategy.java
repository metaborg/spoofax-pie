package mb.statix.referenceretention.stratego;

import mb.nabl2.terms.ITerm;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.scopegraph.Scope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Creates a locked reference, that is, a reference whose syntax and resolution are specified.
 * <p>
 * To call this strategy, use {@code prim("RR_lock_reference", scope)}, where the current term becomes the reference
 * term and the {@code scope} is the scope that the reference resolved to.
 */
public final class RRLockReferenceStrategy extends AbstractPrimitive {
    public static final String NAME = "RR_lock_reference";
    public RRLockReferenceStrategy() {
        super(NAME, 0, 1);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        @Nullable final IStrategoTerm result = eval(env.getFactory(), env.current(), tvars[0]);
        if (result == null) return false;
        env.setCurrent(result);
        return true;
    }

    // TODO: The scope should be an appropriate type, e.g., Scope or something.
    @Nullable private IStrategoTerm eval(ITermFactory termFactory, IStrategoTerm input, IStrategoTerm scope) throws InterpreterException {
        final ITerm input2 = null; // TODO: Get this from the `input` term
        final Scope scope2 = null; // TODO: Get this from the `scope` term
        // TODO: Build RRLockedReference Stratego term
        //RRLockedReference.builder().term(input2).declaration(scope2).build();
        return null;
    }
}

