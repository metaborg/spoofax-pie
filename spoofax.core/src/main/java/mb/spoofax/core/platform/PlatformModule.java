package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@Module
public class PlatformModule {
    @Provides @PlatformScope static ResourceService provideResourceRegistry(Set<ResourceRegistry> registries) {
        if(registries.isEmpty()) {
            throw new RuntimeException("Cannot provide resource service; no resource registries have been set");
        }
        return new DefaultResourceService(registries);
    }

    @Provides @PlatformScope @Named("platform") @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefs() {
        return new HashSet<>();
    }
}
