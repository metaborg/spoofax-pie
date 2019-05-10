package mb.spoofax.intellij;

import dagger.Component;
import mb.pie.dagger.PieModule;
import mb.spoofax.core.platform.FSRegistryModule;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;
import mb.spoofax.intellij.resource.IntellijResourceRegistryModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    FSRegistryModule.class,
    IntellijResourceRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface SpoofaxIntellijComponent extends PlatformComponent {

}
