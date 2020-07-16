package mb.str.spoofax;

import dagger.Component;
import mb.resource.text.TextResourceRegistry;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.spoofax.core.platform.ResourceServiceModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    ResourceRegistriesModule.class,
    ResourceServiceModule.class,
    PlatformPieModule.class
})
public interface PlatformTestComponent extends PlatformComponent {
    TextResourceRegistry getTextResourceRegistry();
}
