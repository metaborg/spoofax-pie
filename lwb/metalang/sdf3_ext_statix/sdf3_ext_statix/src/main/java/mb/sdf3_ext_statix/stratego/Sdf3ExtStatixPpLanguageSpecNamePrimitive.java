package mb.sdf3_ext_statix.stratego;

import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class Sdf3ExtStatixPpLanguageSpecNamePrimitive extends AbstractPrimitive {
    public Sdf3ExtStatixPpLanguageSpecNamePrimitive() {
        super("pp_language_spec_name", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            final Sdf3ExtStatixContext context = AdaptableContext.adaptContextObject(env.contextObject(), Sdf3ExtStatixContext.class);
            env.setCurrent(env.getFactory().makeString(context.strategyAffix));
            return true;
        } catch(RuntimeException e) {
            return false; // Context not available; fail
        }
    }
}
