package mb.spoofax.cli;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformScope;

@PlatformScope
@Component(
    modules = {

    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class
    }
)
public interface SpoofaxCliComponent extends PlatformComponent {
    SpoofaxCli getSpoofaxCmd();
}
