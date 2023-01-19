package mb.statix.referenceretention.stratego;

import mb.statix.referenceretention.statix.RRPlaceholder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.List;

/**
 * Creates a reference retention placeholder, that is, a placeholder of the form {@code [[ <body> | <context> ]]}
 * where {@code <body>} is the syntactic term wrapped by the placeholder and {@code <context>} is the context
 * in which the references in the body must be resolved.
 * <p>
 * For example, when inlining a method body, the body is wrapped in a placeholder and the context is the instance
 * on which the method was called. The {@link RRFixReferencesStrategy} will gradually unwrap the placeholder's body
 * and fix any references it finds using the given context.
 * <p>
 * To call this strategy, use {@code prim("RR_create_placeholder", ctx)}, where the current term becomes the placeholder
 * body and the {@code ctx} becomes the placeholder context.
 */
public final class RRCreatePlaceholderStrategy extends AbstractPrimitive {
    public static final String NAME = "RR_create_placeholder";
    public RRCreatePlaceholderStrategy() {
        super(NAME, 0, 1);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        final IStrategoTerm body = env.current();
        final List<IStrategoTerm> context = ((IStrategoList)tvars[0]).getSubterms();
        @Nullable final IStrategoTerm result = eval(env.getFactory(), body, context);
        if (result == null) return false;
        env.setCurrent(result);
        return true;
    }

    @Nullable private IStrategoTerm eval(ITermFactory termFactory, IStrategoTerm body, List<IStrategoTerm> context) {
        return new RRPlaceholderApplTerm(body, context, termFactory);
    }
}
