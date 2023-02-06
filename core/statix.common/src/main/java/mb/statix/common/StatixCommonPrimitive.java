package mb.statix.common;

import mb.statix.spoofax.SolverMode;
import mb.stratego.common.AdaptException;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;

import java.util.Optional;

public abstract class StatixCommonPrimitive extends AbstractPrimitive  {
    public StatixCommonPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }

    protected Optional<SolverMode> getSolverMode(IContext env) throws InterpreterException, AdaptException {
        Spoofax3StatixProjectConfig config = AdaptableContext.adaptContextObject(env.contextObject(), Spoofax3StatixProjectConfig.class);
        return Optional.of(config.solverMode());
    }
}
