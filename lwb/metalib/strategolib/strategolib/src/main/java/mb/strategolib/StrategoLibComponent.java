package mb.strategolib;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;

@StrategoLibScope
@Component(
    modules = {
        mb.strategolib.StrategoLibModule.class
    },
    dependencies = {
        LoggerComponent.class,
        mb.strategolib.StrategoLibResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface StrategoLibComponent extends BaseStrategoLibComponent {
    StrategoLibUtil getStrategoLibUtil();
}
