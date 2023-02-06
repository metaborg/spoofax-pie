package mb.spoofax.lwb.dynamicloading;

import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.EmptyParticipant;
import mb.spoofax.core.component.SubcomponentRegistry;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicLoadingParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends EmptyParticipant<L, R, P> {
    private final DynamicLoadingModule dynamicLoadingModule;

    protected @Nullable DynamicLoadingComponent component;


    public DynamicLoadingParticipant(DynamicLoadingModule dynamicLoadingModule) {
        this.dynamicLoadingModule = dynamicLoadingModule;
    }


    @Override
    public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "spoofax.lwb.dynamicloading", new Version(0, 1, 0)); // TODO: get real version.
    }

    @Override
    public @Nullable String getCompositionGroup() {
        return "mb.spoofax.lwb";
    }


    @Override
    public DynamicLoadingComponent getTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        if(component != null) return component;

        final DynamicLoadingComponent component = DaggerDynamicLoadingComponent.builder()
            .dynamicLoadingModule(dynamicLoadingModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(baseResourceServiceComponent)
            .platformComponent(platformComponent)
            .build();

        subcomponentRegistry.register(DynamicLoadingComponent.class, component);

        this.component = component;
        return component;
    }

    @Override public void close() {
        if(component != null) {
            component.close();
            component = null;
        }
    }
}
