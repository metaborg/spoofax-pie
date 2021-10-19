package mb.spoofax.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;

import javax.inject.Named;

@Module
public abstract class EclipsePlatformModule {
    @Provides
    @Named("LifecycleParticipantManager")
    @PlatformScope
    static LockRule provideLifecycleParticipantManagerWriteLockRule() {
        return new LockRule("Lifecycle participant manager");
    }

    @Provides
    @Named("LifecycleParticipantManager")
    /* Unscoped: creates a new read lock every time, which is intended. */
    static ReadLockRule provideLifecycleParticipantManagerReadLockRule(@Named("LifecycleParticipantManager") LockRule writeLock) {
        return writeLock.createReadLock();
    }
}
