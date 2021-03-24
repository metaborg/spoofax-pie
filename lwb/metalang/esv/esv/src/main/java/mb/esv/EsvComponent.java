package mb.esv;

import dagger.Component;
import mb.esv.task.spoofax.EsvConfigFunctionWrapper;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;

@EsvScope
@Component(
    modules = {
        EsvModule.class,
    },
    dependencies = {
        LoggerComponent.class,
        EsvResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface EsvComponent extends BaseEsvComponent {
    EsvConfigFunctionWrapper getEsvConfigFunctionWrapper();
}
