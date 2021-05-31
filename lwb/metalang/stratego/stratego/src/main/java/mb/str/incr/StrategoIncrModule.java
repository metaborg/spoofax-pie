package mb.str.incr;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import mb.str.StrategoScope;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleImportService;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import javax.inject.Named;

@Module
public abstract class StrategoIncrModule {
    @Binds @StrategoScope
    public abstract StrategoLanguage bindStrategoLanguage(Spoofax3StrategoLanguage parseStratego);

    @Binds @StrategoScope
    public abstract IOAgentTrackerFactory bindIoAgentTrackerFactory(StrategoIOAgentTrackerFactory ioAgentTrackerFactory);

    @Binds @StrategoScope
    public abstract ResourcePathConverter bindResourcePathConverter(AsStringResourcePathConverter asStringResourcePathConverter);

    @Binds @StrategoScope
    public abstract IModuleImportService bindModuleImportService(ModuleImportService moduleImportService);


    @Provides @StrategoScope
    public static ITermFactory provideTermFactory(@Named("prototype") StrategoRuntime prototypeStrategoRuntime) {
        return new TermFactory(); // HACK: Stratego incremental compiler requires a term factory that does not implement OriginTermFactory.
    }
}
