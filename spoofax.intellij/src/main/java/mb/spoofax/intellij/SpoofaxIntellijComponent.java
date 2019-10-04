package mb.spoofax.intellij;

import com.intellij.openapi.extensions.PluginId;
import dagger.Component;
import mb.pie.dagger.PieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;
import mb.spoofax.intellij.pie.PieRunner;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.spoofax.intellij.resource.IntellijResourceRegistryModule;
import mb.spoofax.intellij.resource.ResourceUtil;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    ResourceRegistriesModule.class,
    IntellijResourceRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface SpoofaxIntellijComponent extends PlatformComponent {
    PieRunner getPieRunner();

    IntellijResourceRegistry getResourceRegistry();

    ResourceUtil getResourceUtil();
}
