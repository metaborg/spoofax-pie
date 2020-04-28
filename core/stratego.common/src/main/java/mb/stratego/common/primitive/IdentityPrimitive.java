package mb.stratego.common.primitive;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class IdentityPrimitive extends AbstractPrimitive {
    public IdentityPrimitive(String name, int strategyArity, int termArity) {
        super(name, strategyArity, termArity);
    }

    public IdentityPrimitive(String name, int strategyArity) {
        this(name, strategyArity, 0);
    }

    public IdentityPrimitive(String name) {
        this(name, 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms) {
        return true;
    }
}
