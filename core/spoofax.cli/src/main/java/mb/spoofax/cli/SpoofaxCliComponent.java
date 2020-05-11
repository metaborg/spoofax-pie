package mb.spoofax.cli;

import dagger.Component;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.spoofax.core.platform.ResourceServiceModule;

import javax.inject.Singleton;

@Singleton @Component(modules = {
    LoggerFactoryModule.class,
    ResourceRegistriesModule.class,
    ResourceServiceModule.class,
    PlatformPieModule.class
})
public interface SpoofaxCliComponent extends PlatformComponent {
    SpoofaxCli getSpoofaxCmd();
}
