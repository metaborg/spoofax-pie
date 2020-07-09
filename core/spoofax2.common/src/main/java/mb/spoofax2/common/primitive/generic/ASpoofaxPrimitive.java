package mb.spoofax2.common.primitive.generic;

import mb.stratego.common.AdaptableContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public abstract class ASpoofaxPrimitive extends AbstractPrimitive {
    public ASpoofaxPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }

    protected abstract @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext context
    ) throws InterpreterException;

    @Override public boolean call(IContext context, Strategy[] svars, IStrategoTerm[] tvars)
        throws InterpreterException {
        final IStrategoTerm current = context.current();
        final ITermFactory termFactory = context.getFactory();
        final @Nullable IStrategoTerm newCurrent = call(current, svars, tvars, termFactory, context);
        if(newCurrent != null) {
            context.setCurrent(newCurrent);
            return true;
        }
        return false;
    }

    protected Spoofax2Context getSpoofax2Context(IContext env) {
        return AdaptableContext.adaptContextObject(env.contextObject(), Spoofax2Context.class);
    }
}
