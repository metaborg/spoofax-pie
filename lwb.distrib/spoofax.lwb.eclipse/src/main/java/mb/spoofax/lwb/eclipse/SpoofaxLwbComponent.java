package mb.spoofax.lwb.eclipse;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.eclipse.dynamicloading.DynamicChangeProcessor;
import mb.spoofax.lwb.eclipse.dynamicloading.DynamicEditorTracker;

@SpoofaxLwbScope
@Component(
    modules = {
        SpoofaxLwbModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        DynamicLoadingComponent.class,
    }
)
public interface SpoofaxLwbComponent extends AutoCloseable {
    DynamicChangeProcessor getDynamicChangeProcessor();

    DynamicEditorTracker getDynamicEditorTracker();

    DynamicLoadingComponent getDynamicLoadingComponent();


    default void start(StaticComponentManager staticComponentManager) {
        getDynamicEditorTracker().register();
        getDynamicChangeProcessor().register();
        getDynamicLoadingComponent().getDynamicLoadGetBaseComponentManager().set(staticComponentManager);
    }

    @Override default void close() {
        getDynamicChangeProcessor().close();
        getDynamicEditorTracker().unregister();
    }
}
