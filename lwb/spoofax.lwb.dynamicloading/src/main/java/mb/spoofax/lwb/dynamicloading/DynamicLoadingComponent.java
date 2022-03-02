package mb.spoofax.lwb.dynamicloading;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;

import java.util.Set;

@DynamicLoadingScope
@Component(
    modules = {
        DynamicLoadingModule.class,
        DynamicLoadingPieModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface DynamicLoadingComponent extends TaskDefsProvider, AutoCloseable {
    DynamicComponentManager getDynamicComponentManager();

    DynamicLoad getDynamicLoad();

    DynamicLoadGetBaseComponentManager getDynamicLoadGetBaseComponentManager();


    @Override @DynamicLoadingQualifier Set<TaskDef<?, ?>> getTaskDefs();

    @Override default void close() {
        getDynamicComponentManager().close();
    }
}
