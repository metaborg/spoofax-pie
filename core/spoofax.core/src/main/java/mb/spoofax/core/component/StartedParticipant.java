package mb.spoofax.core.component;

import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;

public interface StartedParticipant extends AutoCloseable {
    /**
     * Notification that all participants have started, allowing more setup code. Only called after {@link Participant#start} has
     * been called for all participants.
     */
    void started(
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        StaticComponentManager staticComponentManager,
        ComponentManager componentManager
    );

    @Override void close(); // Override with `throws Exception`.
}
