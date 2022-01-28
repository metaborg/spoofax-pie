package mb.spoofax.lwb.dynamicloading;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;

@Module
public class DynamicLoadingModule {
    private final StaticComponentManager staticComponentManager;

    public DynamicLoadingModule(StaticComponentManager staticComponentManager) {
        this.staticComponentManager = staticComponentManager;
    }

    @Provides @DynamicLoadingScope
    DynamicComponentManager provideDynamicComponentManager() {
        return new DynamicComponentManager(staticComponentManager);
    }
}
