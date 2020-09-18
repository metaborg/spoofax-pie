package mb.tiger.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Named;

@Module
public class TigerEclipseModule {
    @Provides @TigerScope
    static EclipseIdentifiers provideEclipseIdentifiers() { return new TigerEclipseIdentifiers(); }

    @Provides @TigerScope @Named("StartupWriteLock")
    static LockRule provideStartupWriteLockRule() { return new LockRule("Tiger startup write lock"); }

    @Provides @TigerScope
    static ReadLockRule provideStartupReadLockRule(@Named("StartupWriteLock") LockRule writeLock) { return new ReadLockRule(writeLock, "Tiger startup read lock"); }
}
