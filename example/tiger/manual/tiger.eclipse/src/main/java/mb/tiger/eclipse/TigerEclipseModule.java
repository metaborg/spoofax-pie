package mb.tiger.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.job.LockRule;

@Module
public class TigerEclipseModule {
    @Provides @LanguageScope
    EclipseIdentifiers provideEclipseIdentifiers() { return new TigerEclipseIdentifiers(); }

    @Provides @LanguageScope
    LockRule provideStartupLockRule() { return new LockRule("Startup write lock"); }
}
