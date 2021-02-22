package mb.spoofax.intellij.log;

import dagger.Module;
import dagger.Provides;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerScope;

@Module
public class IntellijLoggerModule {
    @Provides @LoggerScope LoggerFactory provideLoggerFactory() {
        return new IntellijLoggerFactory();
    }
}

