package mb.statix.common;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

public class LanguageComponentPrimitive extends AbstractPrimitive {
    private final String groupId;
    private final String id;
    private final String version;
    private final String locationUri;

    public LanguageComponentPrimitive(String groupId, String id, String version, String locationUri) {
        super("language_components", 0, 0);
        this.groupId = groupId;
        this.id = id;
        this.version = version;
        this.locationUri = locationUri;
    }

    @Override public boolean call(@NonNull IContext env, @NonNull Strategy[] svars, @NonNull IStrategoTerm[] tvars) {
        final ITermFactory factory = env.getFactory();
        final IStrategoString groupIdTerm = factory.makeString(groupId);
        final IStrategoString idTerm = factory.makeString(id);
        final IStrategoString versionTerm = factory.makeString(version);
        final IStrategoString locationTerm = factory.makeString(locationUri);
        final IStrategoTuple tuple = factory.makeTuple(groupIdTerm, idTerm, versionTerm, locationTerm);
        final IStrategoList list = factory.makeListCons(tuple, factory.makeList());
        env.setCurrent(list);
        return true;
    }
}
