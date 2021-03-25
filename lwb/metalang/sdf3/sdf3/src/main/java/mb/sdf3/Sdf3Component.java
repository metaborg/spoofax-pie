package mb.sdf3;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.task.spoofax.Sdf3SpecConfigFunctionWrapper;
import mb.spoofax.core.platform.PlatformComponent;

@Sdf3Scope
@Component(
    modules = {
        Sdf3Module.class
    },
    dependencies = {
        LoggerComponent.class,
        Sdf3ResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface Sdf3Component extends BaseSdf3Component {
    Sdf3SpecConfigFunctionWrapper getSdf3SpecConfigFunctionWrapper();
}
