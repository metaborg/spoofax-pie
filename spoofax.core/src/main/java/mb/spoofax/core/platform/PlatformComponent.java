package mb.spoofax.core.platform;

import dagger.Component;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;

import javax.inject.Named;
import java.util.Set;

@PlatformScope @Component(modules = {PlatformModule.class}, dependencies = {ResourceRegistryComponent.class})
public interface PlatformComponent {
    ResourceService getResourceService();

    @Named("platform") Set<TaskDef<?, ?>> getTaskDefs();
}
