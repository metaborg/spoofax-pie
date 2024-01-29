package mb.spoofax.eclipse.log;

import dagger.Module;
import dagger.Provides;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerScope;

@Module
public class EclipseLoggerModule {
    @Provides @LoggerScope LoggerFactory provideLoggerFactory() {
        return new EclipseLoggerFactory();
    }
}

