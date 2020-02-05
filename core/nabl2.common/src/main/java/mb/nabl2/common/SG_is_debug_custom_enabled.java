package mb.nabl2.common;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_is_debug_custom_enabled extends AbstractPrimitive {
    public SG_is_debug_custom_enabled() {
        super(SG_is_debug_custom_enabled.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(@NonNull IContext env, @NonNull Strategy[] svars, @NonNull IStrategoTerm[] tvars) {
        return false;
    }
}
