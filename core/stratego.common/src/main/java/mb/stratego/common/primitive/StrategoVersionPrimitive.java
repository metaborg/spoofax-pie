package mb.stratego.common.primitive;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class StrategoVersionPrimitive extends AbstractPrimitive {
    public StrategoVersionPrimitive() {
        super("stratego_version", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        env.setCurrent(env.getFactory().makeString("2"));
        return true;
    }
}
