package mb.spoofax.cli;

import dagger.Component;
import mb.pie.dagger.PieModule;
import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.platform.FSRegistryModule;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;

import javax.inject.Singleton;

@Singleton @Component(modules = {
    LoggerFactoryModule.class,
    FSRegistryModule.class,
    StringResourceRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface SpoofaxCliComponent extends PlatformComponent {
    StringResourceRegistry getStringResourceRegistry();

    SpoofaxCli getSpoofaxCmd();
}
