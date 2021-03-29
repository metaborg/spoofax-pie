package mb.statix;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.task.spoofax.StatixConfigFunctionWrapper;

@StatixScope
@Component(
    modules = {
        StatixModule.class
    },
    dependencies = {
        LoggerComponent.class,
        StatixResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface StatixComponent extends BaseStatixComponent {
    StatixConfigFunctionWrapper getStatixConfigFunctionWrapper();
}
