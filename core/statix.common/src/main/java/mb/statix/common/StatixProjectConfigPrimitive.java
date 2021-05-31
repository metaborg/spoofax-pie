package mb.statix.common;

import mb.nabl2.terms.stratego.StrategoBlob;
import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.StatixProjectConfig;
import mb.stratego.common.AdaptException;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class StatixProjectConfigPrimitive extends AbstractPrimitive {
    public StatixProjectConfigPrimitive() {
        super("STX_project_config", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        IStatixProjectConfig config;
        try {
            config = AdaptableContext.adaptContextObject(env.contextObject(), IStatixProjectConfig.class);
        } catch(AdaptException e) {
            config = StatixProjectConfig.NULL;
        }
        env.setCurrent(new StrategoBlob(config));
        return true;
    }
}
