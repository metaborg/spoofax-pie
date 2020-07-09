package mb.spoofax2.common.primitive.generic;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public abstract class ASpoofaxContextPrimitive extends ASpoofaxPrimitive {
    public ASpoofaxContextPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }

    protected abstract @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext strategoContext,
        Spoofax2Context context
    ) throws InterpreterException;

    protected @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext strategoContext
    ) throws InterpreterException {
        final Spoofax2Context context = getSpoofax2Context(strategoContext);
        return call(current, svars, tvars, termFactory, strategoContext, context);
    }
}
