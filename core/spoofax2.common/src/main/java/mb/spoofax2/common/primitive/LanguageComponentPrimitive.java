package mb.spoofax2.common.primitive;

import mb.spoofax2.common.primitive.generic.ASpoofaxContextPrimitive;
import mb.spoofax2.common.primitive.generic.Spoofax2Context;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

public class LanguageComponentPrimitive extends ASpoofaxContextPrimitive {
    public LanguageComponentPrimitive() {
        super("language_components", 0, 0);
    }

    @Override public @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext strategoContext,
        Spoofax2Context context
    ) {
        final IStrategoString groupIdTerm = termFactory.makeString(context.languageGroupId);
        final IStrategoString idTerm = termFactory.makeString(context.languageId);
        final IStrategoString versionTerm = termFactory.makeString(context.languageVersion);
        final IStrategoString locationTerm = termFactory.makeString(context.languagePathString.toString());
        final IStrategoTuple tuple = termFactory.makeTuple(groupIdTerm, idTerm, versionTerm, locationTerm);
        return termFactory.makeListCons(tuple, termFactory.makeList());
    }
}
