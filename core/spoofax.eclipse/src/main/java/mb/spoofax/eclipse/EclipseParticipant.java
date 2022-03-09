package mb.spoofax.eclipse;

import mb.spoofax.core.component.Participant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;

public interface EclipseParticipant extends Participant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> {
    @Override default Class<? super EclipseLoggerComponent> getRequiredLoggerComponentClass() {
        return EclipseLoggerComponent.class;
    }

    @Override default Class<? super EclipseResourceServiceComponent> getRequiredBaseResourceServiceComponentClass() {
        return EclipseResourceServiceComponent.class;
    }

    @Override default Class<? super EclipsePlatformComponent> getRequiredPlatformComponentClass() {
        return EclipsePlatformComponent.class;
    }
}
