package mb.spoofax.cli;

import dagger.Component;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.core.platform.ResourceServiceComponent;

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
public interface SpoofaxCliComponent extends PlatformComponent {
    SpoofaxCli getSpoofaxCmd();
}
