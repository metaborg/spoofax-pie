package mb.spoofax2.common.primitive;

import mb.spoofax2.common.primitive.generic.ASpoofaxPrimitive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class LanguagePrimitive extends ASpoofaxPrimitive {
    public LanguagePrimitive() {
        super("language", 0, 0);
    }

    @Override public @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext context
    ) {
        return termFactory.makeString(getSpoofax2LanguageContext(context).languageId);
    }
}
