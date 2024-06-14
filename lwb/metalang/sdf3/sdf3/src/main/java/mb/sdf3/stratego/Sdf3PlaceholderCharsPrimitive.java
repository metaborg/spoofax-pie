package mb.sdf3.stratego;

import java.util.Objects;

import org.metaborg.util.tuple.Tuple2;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.common.option.Option;
import mb.stratego.common.AdaptException;
import mb.stratego.common.AdaptableContext;

public class Sdf3PlaceholderCharsPrimitive extends AbstractPrimitive {
    public Sdf3PlaceholderCharsPrimitive() {
        super("SSL_EXT_placeholder_chars", 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            final Sdf3Context context = AdaptableContext.adaptContextObject(env.contextObject(), Sdf3Context.class);
            final Option<Tuple2<String, String>> placeholders = context.placeholders;
            if(placeholders.isNone()) {
                return false;
            }
            final Tuple2<String, String> placeholderChars = Objects.requireNonNull(placeholders.get());
            final ITermFactory f = env.getFactory();
            env.setCurrent(f.makeTuple(f.makeString(placeholderChars._1()), f.makeString(placeholderChars._2())));
            return true;
        } catch(AdaptException e) {
            return false; // Context not available; fail
        }
    }
}
