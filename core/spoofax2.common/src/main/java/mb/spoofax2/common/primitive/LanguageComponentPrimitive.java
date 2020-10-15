package mb.spoofax2.common.primitive;

import mb.spoofax2.common.primitive.generic.ASpoofaxPrimitive;
import mb.spoofax2.common.primitive.generic.Spoofax2LanguageContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

public class LanguageComponentPrimitive extends ASpoofaxPrimitive {
    public LanguageComponentPrimitive() {
        super("language_components", 0, 0);
    }

    @Override public @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext context
    ) {
        final Spoofax2LanguageContext languageContext = getSpoofax2LanguageContext(context);
        final IStrategoString groupIdTerm = termFactory.makeString(languageContext.languageGroupId);
        final IStrategoString idTerm = termFactory.makeString(languageContext.languageId);
        final IStrategoString versionTerm = termFactory.makeString(languageContext.languageVersion);
        final IStrategoString locationTerm = termFactory.makeString(languageContext.languagePath.asString());
        final IStrategoTuple tuple = termFactory.makeTuple(groupIdTerm, idTerm, versionTerm, locationTerm);
        return termFactory.makeListCons(tuple, termFactory.makeList());
    }
}
