package mb.dynamix;

import dagger.Component;
import mb.dynamix.task.spoofax.DynamixConfigFunctionWrapper;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;

@DynamixScope
@Component(
    modules = {
        DynamixModule.class
    },
    dependencies = {
        LoggerComponent.class,
        DynamixResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface DynamixComponent extends BaseDynamixComponent {
    DynamixConfigFunctionWrapper getDynamixConfigFunctionWrapper();
}
