package mb.spoofax.lwb.dynamicloading;

import dagger.Component;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;

import java.util.Set;

@DynamicLoadingScope
@Component(
    modules = {
        DynamicLoadingModule.class,
        DynamicLoadingPieModule.class
    },
    dependencies = {

    }
)
public interface DynamicLoadingComponent extends TaskDefsProvider, AutoCloseable {
    DynamicComponentManager getDynamicComponentManager();

    DynamicLoad getDynamicLoad();


    @Override @DynamicLoadingQualifier Set<TaskDef<?, ?>> getTaskDefs();

    @Override default void close() {
        getDynamicComponentManager().close();
    }
}
