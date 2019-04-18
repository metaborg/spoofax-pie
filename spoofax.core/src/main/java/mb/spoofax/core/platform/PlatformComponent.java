package mb.spoofax.core.platform;

import dagger.Component;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.pie.dagger.PieModule;
import mb.resource.ResourceService;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    FSRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface PlatformComponent {
    LoggerFactory getLoggerFactory();

    ResourceService getResourceService();

    Pie getPie();
}
