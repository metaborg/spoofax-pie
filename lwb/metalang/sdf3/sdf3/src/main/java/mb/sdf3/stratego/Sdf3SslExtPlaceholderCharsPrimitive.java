package mb.sdf3.stratego;

import org.spoofax.interpreter.library.AbstractPrimitive;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * Returns a tuple with the prefix and suffix, respectively, to use to parse/pretty-print placeholders.
 */
public final class Sdf3SslExtPlaceholderCharsPrimitive extends AbstractPrimitive {

    public Sdf3SslExtPlaceholderCharsPrimitive() {
        super("SSL_EXT_placeholder_chars", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        final ITermFactory factory = env.getFactory();
        try {
            final Sdf3Context context = AdaptableContext.adaptContextObject(env.contextObject(), Sdf3Context.class);
            env.setCurrent(factory.makeTuple(
                factory.makeString(context.placeholderPrefix),
                factory.makeString(context.placeholderSuffix)
            ));
            return true;
        } catch(RuntimeException e) {
            return false; // Context not available; fail
        }
    }
}
