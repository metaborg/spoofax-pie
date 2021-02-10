package mb.spoofax.core.platform;

import dagger.Component;
import mb.log.api.LoggerFactory;
import mb.pie.api.PieBuilder;

@PlatformScope
@Component(
    modules = {
        LoggerFactoryModule.class,
        PlatformPieModule.class
    },
    dependencies = {
        ResourceServiceComponent.class
    }
)
public interface PlatformComponent {
    LoggerFactory getLoggerFactory();

    @Platform PieBuilder newPieBuilder();
}
