package mb.tiger.spoofax;

import mb.common.util.ListView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.Participant;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TigerParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements Participant<L, R, P> {
    protected @Nullable TigerResourcesComponent resourcesComponent;
    protected @Nullable TigerComponent component;


    @Override public Coordinate getCoordinate() {
        return new Coordinate(
            "org.metaborg",
            "tiger",
            new Version(0, 1, 0)
        );
    }

    @Override public @Nullable String getGroup() {
        return null;
    }

    @Override public ListView<String> getLanguageFileExtensions() {
        return ListView.copyOf(TigerInstance.extensions);
    }


    protected TigerResourcesModule createResourcesModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent
    ) {
        return new TigerResourcesModule();
    }

    protected void customizeResourcesModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        TigerResourcesModule module
    ) {}

    protected void customizeResourcesComponentBuilder(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        DaggerTigerResourcesComponent.Builder builder
    ) {}

    @Override
    public TigerResourcesComponent getResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent
    ) {
        if(resourcesComponent == null) {
            final TigerResourcesModule module = createResourcesModule(loggerComponent, baseResourceServiceComponent, platformComponent);
            customizeResourcesModule(loggerComponent, baseResourceServiceComponent, platformComponent, module);
            final DaggerTigerResourcesComponent.Builder builder = DaggerTigerResourcesComponent.builder()
                .tigerResourcesModule(module);
            customizeResourcesComponentBuilder(loggerComponent, baseResourceServiceComponent, platformComponent, builder);
            resourcesComponent = builder.build();
        }
        return resourcesComponent;
    }


    @Override
    public TaskDefsProvider getTaskDefsProvider(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent) {
        return getLanguageComponent(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent);
    }


    protected TigerModule createModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent
    ) {
        return new TigerModule();
    }

    protected void customizeModule(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        TigerModule module
    ) {}

    @Override
    public LanguageComponent getLanguageComponent(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent) {
        if(component == null) {
            final TigerModule module = createModule(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent);
            customizeModule(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent, module);
            final DaggerTigerComponent.Builder builder = DaggerTigerComponent.builder()
                .tigerModule(module)
                .loggerComponent(loggerComponent)
                .tigerResourcesComponent(getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent))
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent);
            component = builder.build();
        }
        return component;
    }


    @Override
    public void start(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent, PieComponent pieComponent) {

    }

    @Override public void close() {
        if(component != null) {
            component.close();
            component = null;
        }
        resourcesComponent = null;
    }
}
