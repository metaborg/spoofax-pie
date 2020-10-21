package mb.spoofax.intellij;

import dagger.Component;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;
import mb.spoofax.intellij.pie.PieRunner;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.spoofax.intellij.resource.IntellijResourceRegistryModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    ResourceRegistriesModule.class,
    IntellijResourceRegistryModule.class,
    ResourceServiceModule.class,
    PlatformPieModule.class
})
public interface SpoofaxIntellijComponent extends PlatformComponent {
    PieRunner getPieRunner();

    IntellijResourceRegistry getResourceRegistry();
}
