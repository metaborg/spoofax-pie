package mb.spoofax.runtime.nabl;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_is_debug_resolution_enabled extends AbstractPrimitive {
    public SG_is_debug_resolution_enabled() {
        super(SG_is_debug_resolution_enabled.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        return false;
    }
}
