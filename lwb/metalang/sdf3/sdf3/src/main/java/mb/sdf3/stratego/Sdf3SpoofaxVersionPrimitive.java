package mb.sdf3.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class Sdf3SpoofaxVersionPrimitive extends AbstractPrimitive {
    public Sdf3SpoofaxVersionPrimitive() {
        super("spoofax_version", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        env.setCurrent(env.getFactory().makeInt(3));
        return true;
    }
}
