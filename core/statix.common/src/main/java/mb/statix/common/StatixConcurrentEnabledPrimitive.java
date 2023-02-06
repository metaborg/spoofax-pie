package mb.statix.common;

import mb.stratego.common.AdaptException;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;


public class StatixConcurrentEnabledPrimitive extends StatixCommonPrimitive {

    public StatixConcurrentEnabledPrimitive() {
        super("STX_is_concurrent_enabled", 0, 0);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            return getSolverMode(env).map(m -> m.concurrent).orElse(false);
        } catch(AdaptException e) {
            return false;
        }
    }
}
