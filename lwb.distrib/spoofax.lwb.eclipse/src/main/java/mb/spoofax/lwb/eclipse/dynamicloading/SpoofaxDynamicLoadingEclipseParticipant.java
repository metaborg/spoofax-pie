package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.common.util.ListView;
import mb.log.api.Level;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.pie.serde.fst.FstSerde;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.fs.FSResource;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.CompositeComponentManager;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.component.SubcomponentRegistry;
import mb.spoofax.eclipse.EclipseParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingModule;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingParticipant;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spoofax.lwb.eclipse.SpoofaxLwbCompilerUtil;
import mb.spoofax.lwb.eclipse.SpoofaxLwbPlugin;
import mb.spt.SptComponent;
import mb.spt.dynamicloading.DynamicLanguageUnderTestProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IPath;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SpoofaxDynamicLoadingEclipseParticipant extends DynamicLoadingParticipant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> implements EclipseParticipant {
    public SpoofaxDynamicLoadingEclipseParticipant() {
        super(
            // NOTE: this customizes the PIE runtimes for dynamically loaded languages.
            new DynamicLoadingModule(
                PieBuilderImpl::new,
                (loggerFactory, classLoader) -> new FstSerde(classLoader) // Use Fst Serde implementation for better serialization performance.
            )
                .addPieModuleCustomizers(pieModule -> {
                    // Use logging and metrics tracer to create build logs.
                    pieModule.withTracerFactory(loggerFactory ->
                        new LoggingTracer(loggerFactory, Level.Info, Level.None, Level.None, Level.None, 1024, new MetricsTracer(true))
                    );
                }));
    }

    @Override
    public ListView<CoordinateRequirement> getDependencies() {
        final ArrayList<CoordinateRequirement> dependencies = new ArrayList<>();
        super.getDependencies().addAllTo(dependencies);
        dependencies.add(new CoordinateRequirement("org.metaborg", "spt"));
        dependencies.add(new CoordinateRequirement("org.metaborg", "spoofax.lwb.compiler"));
        return ListView.of(dependencies);
    }

    @Override
    public DynamicLoadingComponent getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        final DynamicLoadingComponent dynamicLoadingComponent = super.getTaskDefsProvider(
            loggerComponent,
            baseResourceServiceComponent,
            resourceServiceComponent,
            platformComponent,
            subcomponentRegistry,
            dependencyResolver
        );
        // Set dynamic component manager in several places.
        final DynamicComponentManager dynamicComponentManager = dynamicLoadingComponent.getDynamicComponentManager();
        dependencyResolver.getOneSubcomponent(SptComponent.class).unwrap().getLanguageUnderTestProviderWrapper().set(new DynamicLanguageUnderTestProvider(
            dynamicComponentManager,
            dynamicLoadingComponent.getDynamicLoad(),
            SpoofaxLwbCompilerUtil::dynamicLoadSupplierOutputSupplier
        ));
        return dynamicLoadingComponent;
    }

    @Override
    public @Nullable Consumer<RootPieModule> getPieModuleCustomizer() {
        // NOTE: this customizes the PIE runtime for the "mb.spoofax.lwb" composition group (i.e., statically loaded
        // Spoofax LWB languages).
        return pieModule -> {
            // Use Fst Serde implementation for better serialization performance.
            pieModule.withSerdeFactory(loggerFactory -> new FstSerde());
            // Override store factory so that state gets serialized to the `pieStore` file.
            pieModule.withStoreFactory((serde, resourceService, loggerFactory) -> {
                final IPath statePath = SpoofaxLwbPlugin.getPlugin().getStateLocation();
                final FSResource stateDir = ResourceUtil.toFsResource(statePath);
                return SerializingStoreBuilder.ofInMemoryStore(serde)
                    .withResourceStorage(stateDir.appendRelativePath("pieStore"))
                    .withLoggingDeserializeFailHandler(loggerFactory)
                    .build();
            });
            // Use logging and metrics tracer to create build logs.
            pieModule.withTracerFactory(loggerFactory ->
                new LoggingTracer(loggerFactory, Level.Info, Level.None, Level.None, Level.None, 1024, new MetricsTracer(true))
            );
        };
    }

    @Override
    public void started(
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        StaticComponentManager staticComponentManager,
        ComponentManager componentManager
    ) {
        super.started(resourceServiceComponent, pieComponent, staticComponentManager, componentManager);
        final CompositeComponentManager compositeComponentManager = new CompositeComponentManager(componentManager, component.getDynamicComponentManager());
        componentManager.getOneSubcomponent(SpoofaxLwbCompilerComponent.class).unwrap().getSpoofaxLwbCompilerComponentManagerWrapper().set(compositeComponentManager);
        SpoofaxPlugin.setComponentManager(compositeComponentManager);
    }
}
