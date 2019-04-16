package mb.spoofax.cmd;

import dagger.Component;
import mb.pie.dagger.PieModule;
import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.platform.FSRegistryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;

import javax.inject.Singleton;

@Singleton @Component(modules = {
    FSRegistryModule.class,
    StringRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface SpoofaxCmdComponent extends PlatformComponent {
    StringResourceRegistry getStringResourceRegistry();

    SpoofaxCmd getSpoofaxCmd();
}
