package mb.spoofax.core.platform;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;

@PlatformScope
@Component(
    modules = {

    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class
    }
)
public interface PlatformComponent extends AutoCloseable {
    @Override default void close() throws Exception {
        // Override to make Dagger not treat this as a component method.
    }
}
