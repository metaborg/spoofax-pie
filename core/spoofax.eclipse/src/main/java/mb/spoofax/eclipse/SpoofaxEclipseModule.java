package mb.spoofax.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.pie.api.Logger;
import mb.pie.runtime.logger.NoopLogger;

import javax.inject.Singleton;

@Module
public class SpoofaxEclipseModule {
    @Provides @Singleton Logger providePieLogger() {
        // return StreamLogger.nonVerbose();
        return new NoopLogger();
    }
}
