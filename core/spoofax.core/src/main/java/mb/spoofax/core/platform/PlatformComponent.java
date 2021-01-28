package mb.spoofax.core.platform;

import dagger.Component;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.resource.ResourceService;
import mb.resource.text.TextResourceRegistry;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    ResourceRegistriesModule.class,
    ResourceServiceModule.class,
    PlatformPieModule.class
})
public interface PlatformComponent {
    LoggerFactory getLoggerFactory();

    @Platform ResourceService getResourceService();

    @Platform TextResourceRegistry getTextResourceRegistry();

    @Platform PieBuilder getPieBuilder();
}
