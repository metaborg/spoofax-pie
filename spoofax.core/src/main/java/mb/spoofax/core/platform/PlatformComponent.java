package mb.spoofax.core.platform;

import dagger.Component;
import mb.pie.api.Pie;
import mb.pie.dagger.PieModule;
import mb.resource.ResourceService;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    FSRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface PlatformComponent {
    ResourceService getResourceService();

    Pie getPie();
}
