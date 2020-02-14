package mb.constraint.common.stratego;

import mb.constraint.common.ConstraintAnalyzerContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Adapted from from `org.metaborg.spoofax.core`: org.metaborg.spoofax.core.stratego.primitive.constraint.C_get_resource_analysis.
public abstract class ConstraintContextPrimitive extends AbstractPrimitive {
    public ConstraintContextPrimitive(String name) {
        super(name, 0, 0);
    }

    @Override public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
        throws InterpreterException {
        final ConstraintAnalyzerContext context = constraintContext(env);
        final IStrategoTerm term = env.current();
        final List<IStrategoTerm> terms = Arrays.asList(tvars);
        final Optional<? extends IStrategoTerm> result;
        result = call(context, term, terms, env.getFactory());
        return result.map(t -> {
            env.setCurrent(t);
            return true;
        }).orElse(false);
    }

    protected abstract Optional<? extends IStrategoTerm> call(ConstraintAnalyzerContext context, IStrategoTerm term,
        List<IStrategoTerm> terms, ITermFactory factory) throws InterpreterException;

    private ConstraintAnalyzerContext constraintContext(IContext env) throws InterpreterException {
        final Object contextObj = env.contextObject();
        if(contextObj == null) {
            throw new InterpreterException("No context present");
        }
        if(!(contextObj instanceof ConstraintAnalyzerContext)) {
            throw new InterpreterException("Context does not implement ConstraintAnalyzerContext");
        }
        return (ConstraintAnalyzerContext)env.contextObject();
    }
}
