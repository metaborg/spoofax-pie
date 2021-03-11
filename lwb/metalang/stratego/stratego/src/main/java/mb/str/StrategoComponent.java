package mb.str;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.config.StrategoConfigModule;
import mb.str.config.StrategoConfigurator;
import mb.str.incr.StrategoIncrModule;

@StrategoScope
@Component(
    modules = {
        StrategoModule.class,
        StrategoIncrModule.class,
        StrategoConfigModule.class
    },
    dependencies = {
        LoggerComponent.class,
        StrategoResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface StrategoComponent extends GeneratedStrategoComponent {
    StrategoConfigurator getStrategoConfigurator();
}
