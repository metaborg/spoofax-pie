package mb.str.spoofax;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.spoofax.config.StrategoConfigModule;
import mb.str.spoofax.config.StrategoConfigurator;
import mb.str.spoofax.incr.StrategoIncrModule;

@StrategoScope
@Component(
    modules = {StrategoModule.class, JavaTasksModule.class, StrategoIncrModule.class, StrategoConfigModule.class},
    dependencies = {PlatformComponent.class}
)
public interface StrategoComponent extends GeneratedStrategoComponent {
    StrategoConfigurator getStrategoConfigurator();
}
