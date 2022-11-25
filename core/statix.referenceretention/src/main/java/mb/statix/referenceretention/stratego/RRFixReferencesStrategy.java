package mb.statix.referenceretention.stratego;

import mb.statix.referenceretention.strategies.runtime.RRStrategoContext;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Invokes the Tego strategy {@link mb.statix.referenceretention.strategies.runtime.UnwrapOrFixAllReferencesStrategy}
 * to unwrap the placeholder body and fix all references.
 */
public final class RRFixReferencesStrategy extends AbstractPrimitive {
    public static final String NAME = "RR_fix_references";
    public RRFixReferencesStrategy() {
        super(NAME, 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            final RRStrategoContext context = AdaptableContext.adaptContextObject(env.contextObject(), RRStrategoContext.class);
            // Return the name, for debugging
            final String runtimeName = "TegoRuntime:" + context.tegoRuntime;
            System.out.println(runtimeName);
            env.setCurrent(env.getFactory().makeString(runtimeName));
            return true;
        } catch(RuntimeException e) {
            return false; // Context not available; fail
        }
    }
}
