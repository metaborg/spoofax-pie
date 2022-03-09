package mb.spoofax.core.component;

import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;

public interface StartedParticipantFactory extends AutoCloseable {
    @Override void close(); // Override without `throws Exception`.
}
