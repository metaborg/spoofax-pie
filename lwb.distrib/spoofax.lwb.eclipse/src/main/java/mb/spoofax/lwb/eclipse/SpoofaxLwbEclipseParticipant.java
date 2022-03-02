package mb.spoofax.lwb.eclipse;

import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.EmptyParticipant;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.eclipse.EclipseParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxLwbEclipseParticipant extends EmptyParticipant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> implements EclipseParticipant {
    private @Nullable SpoofaxLwbComponent component;

    @Override
    public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "spoofax.lwb.eclipse", new Version(0, 1, 0)); // TODO: get real version
    }

    @Override
    public void started(
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        StaticComponentManager staticComponentManager,
        ComponentManager componentManager
    ) {
        if(component != null) return;
        component = DaggerSpoofaxLwbComponent.builder()
            .loggerComponent(staticComponentManager.getLoggerComponent())
            .resourceServiceComponent(resourceServiceComponent)
            .dynamicLoadingComponent(componentManager.getOneSubcomponent(DynamicLoadingComponent.class).unwrap())
            .build();
        component.start(staticComponentManager);
    }

    @Override public void close() {
        if(component != null) {
            component.close();
            component = null;
        }
    }
}
