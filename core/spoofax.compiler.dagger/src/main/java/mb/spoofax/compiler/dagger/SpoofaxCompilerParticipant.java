package mb.spoofax.compiler.dagger;

import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.EmptyParticipant;
import mb.spoofax.core.component.SubcomponentRegistry;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxCompilerParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends EmptyParticipant<L, R, P> {
    private final SpoofaxCompilerModule module;

    private SpoofaxCompilerComponent component;


    public SpoofaxCompilerParticipant(SpoofaxCompilerModule module) {
        this.module = module;
    }


    @Override
    public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "spoofax.compiler", new Version(0, 1, 0)); // TODO: get actual version
    }

    @Override
    public @Nullable String getCompositionGroup() {
        return "mb.spoofax.lwb";
    }


    @Override
    public @Nullable TaskDefsProvider getTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        if(component == null) {
            component = DaggerSpoofaxCompilerComponent.builder()
                .spoofaxCompilerModule(module)
                .loggerComponent(loggerComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .build();
            subcomponentRegistry.register(SpoofaxCompilerComponent.class, component);
        }
        return component;
    }
}
