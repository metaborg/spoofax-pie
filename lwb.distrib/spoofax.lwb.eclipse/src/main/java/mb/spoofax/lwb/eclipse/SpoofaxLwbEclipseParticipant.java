package mb.spoofax.lwb.eclipse;

import mb.common.util.ListView;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.EmptyParticipant;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.component.SubcomponentRegistry;
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

    @Override public ListView<CoordinateRequirement> getDependencies() {
        return ListView.of(new CoordinateRequirement("org.metaborg", "spoofax.lwb.dynamicloading"));
    }

    @Override
    public @Nullable TaskDefsProvider getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        if(this.component != null) return null;
        final SpoofaxLwbComponent component = DaggerSpoofaxLwbComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .dynamicLoadingComponent(dependencyResolver.getOneSubcomponent(DynamicLoadingComponent.class).unwrap())
            .build();
        subcomponentRegistry.register(SpoofaxLwbComponent.class, component);
        this.component = component;
        return null;
    }

    @Override
    public void started(
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        StaticComponentManager staticComponentManager,
        ComponentManager componentManager
    ) {
        component.start(staticComponentManager);
    }

    @Override public void close() {
        if(component != null) {
            component.close();
            component = null;
        }
    }
}
