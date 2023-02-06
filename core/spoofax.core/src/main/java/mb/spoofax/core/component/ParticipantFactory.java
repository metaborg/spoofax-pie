package mb.spoofax.core.component;

import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;

public interface ParticipantFactory<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends StartedParticipantFactory {
    Participant<L, R, P> create();
}
