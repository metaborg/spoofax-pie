package mb.gpp;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;

@GppScope
@Component(
    modules = {
        mb.gpp.GppModule.class
    },
    dependencies = {
        LoggerComponent.class,
        mb.gpp.GppResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface GppComponent extends BaseGppComponent {
    GppUtil getGppUtil();
}
