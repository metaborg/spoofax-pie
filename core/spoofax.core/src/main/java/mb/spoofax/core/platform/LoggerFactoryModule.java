package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import mb.log.api.LoggerFactory;

import javax.inject.Singleton;

@Module
public class LoggerFactoryModule {
    private final LoggerFactory loggerFactory;

    public LoggerFactoryModule(LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Provides @PlatformScope LoggerFactory provideLoggerFactory() {
        return loggerFactory;
    }
}
