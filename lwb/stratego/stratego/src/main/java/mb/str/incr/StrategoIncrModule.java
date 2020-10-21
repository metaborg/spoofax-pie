package mb.str.incr;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import mb.str.StrategoScope;
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
    public abstract ParseStratego bindParseStratego(Spoofax3ParseStratego parseStratego);

    @Binds @StrategoScope
    public abstract IOAgentTrackerFactory bindIOAgentTrackerFactory(StrategoIOAgentTrackerFactory ioAgentTrackerFactory);

    @Binds @StrategoScope
    public abstract Backend.ResourcePathConverter bindResourcePathConverter(AsStringResourcePathConverter asStringResourcePathConverter);


    @Provides @StrategoScope
    public static ITermFactory provideTermFactory(@Named("prototype") StrategoRuntime prototypeStrategoRuntime) {
        return new TermFactory(); // HACK: Stratego incremental compiler requires a term factory that does not implement OriginTermFactory.
    }
}
