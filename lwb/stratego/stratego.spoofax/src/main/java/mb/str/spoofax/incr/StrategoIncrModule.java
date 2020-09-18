package mb.str.spoofax.incr;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.str.spoofax.StrategoScope;
import mb.stratego.build.strincr.Backend;
import mb.stratego.build.strincr.ParseStratego;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import javax.inject.Named;

@Module
public abstract class StrategoIncrModule {
    @Binds @StrategoScope
    public abstract ParseStratego bindParseStratego(@LanguageScope Spoofax3ParseStratego parseStratego);

    @Binds @StrategoScope
    public abstract IOAgentTrackerFactory bindIOAgentTrackerFactory(@LanguageScope StrategoIOAgentTrackerFactory ioAgentTrackerFactory);

    @Binds @StrategoScope
    public abstract Backend.ResourcePathConverter bindResourcePathConverter(@LanguageScope AsStringResourcePathConverter asStringResourcePathConverter);


    @Provides @StrategoScope
    public static ITermFactory provideTermFactory(@Named("prototype") StrategoRuntime prototypeStrategoRuntime) {
        return new TermFactory(); // HACK: Stratego incremental compiler requires a term factory that does not implement OriginTermFactory.
    }
}
