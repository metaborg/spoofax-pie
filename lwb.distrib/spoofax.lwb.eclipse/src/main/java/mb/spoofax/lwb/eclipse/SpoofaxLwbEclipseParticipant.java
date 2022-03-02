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

public class SpoofaxLwbEclipseParticipant extends EmptyParticipant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> implements EclipseParticipant {
    private static SpoofaxLwbComponent component;

    public SpoofaxLwbComponent getComponent() {
        if(component == null) {
            throw new RuntimeException("SpoofaxLwbComponent has not been initialized yet or has been deinitialized");
        }
        return component;
    }

    @Override public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "spoofax.lwb.eclipse", new Version(0, 1, 0)); // TODO: get real version
    }

    @Override
    public void started(
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        StaticComponentManager staticComponentManager,
        ComponentManager componentManager
    ) {
        component = DaggerSpoofaxLwbComponent.builder()
            .loggerComponent(staticComponentManager.getLoggerComponent())
            .resourceServiceComponent(resourceServiceComponent)
            .dynamicLoadingComponent(componentManager.getOneSubcomponent(DynamicLoadingComponent.class).unwrap())
            .build();
        component.start(staticComponentManager);
    }
}
