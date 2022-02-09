package mb.spoofax.lwb.dynamicloading;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;

@Module
public class DynamicLoadingModule {
    private final DynamicComponentManager dynamicComponentManager;

    public DynamicLoadingModule(DynamicComponentManager dynamicComponentManager) {
        this.dynamicComponentManager = dynamicComponentManager;
    }

    @Provides @DynamicLoadingScope
    DynamicComponentManager provideDynamicComponentManager() {
        return dynamicComponentManager;
    }
}
