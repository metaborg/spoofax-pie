package mb.spoofax.lwb.eclipse;

import mb.cfg.eclipse.CfgLanguageFactory;
import mb.esv.eclipse.EsvLanguageFactory;
import mb.libspoofax2.eclipse.LibSpoofax2LanguageFactory;
import mb.libstatix.eclipse.LibStatixLanguageFactory;
import mb.pie.api.TaskDef;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.resource.dagger.EmptyResourceRegistriesProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.eclipse.Sdf3LanguageFactory;
import mb.spoofax.eclipse.EclipseLifecycleParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import mb.spoofax.lwb.dynamicloading.DaggerDynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingPieModule;
import mb.statix.eclipse.StatixLanguageFactory;
import mb.str.eclipse.StrategoLanguageFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import java.util.HashSet;

public class SpoofaxLwbLifecycleParticipant implements EclipseLifecycleParticipant {
    private static @Nullable SpoofaxLwbLifecycleParticipant instance;

    private SpoofaxLwbLifecycleParticipant() {}

    public static SpoofaxLwbLifecycleParticipant getInstance() {
        if(instance == null) {
            instance = new SpoofaxLwbLifecycleParticipant();
        }
        return instance;
    }

    public static class Factory implements IExecutableExtensionFactory {
        @Override public SpoofaxLwbLifecycleParticipant create() {
            return SpoofaxLwbLifecycleParticipant.getInstance();
        }
    }


    private @Nullable ResourceServiceComponent resourceServiceComponent;
    private @Nullable Spoofax3Compiler spoofax3Compiler;
    private @Nullable DynamicLoadingComponent dynamicLoadingComponent;
    private @Nullable PieComponent pieComponent;
    private @Nullable SpoofaxLwbComponent spoofaxLwbComponent;


    public ResourceServiceComponent getResourceServiceComponent() {
        if(resourceServiceComponent == null) {
            throw new RuntimeException("ResourceServiceComponent has not been initialized yet or has been closed");
        }
        return resourceServiceComponent;
    }

    public Spoofax3Compiler getSpoofax3Compiler() {
        if(spoofax3Compiler == null) {
            throw new RuntimeException("Spoofax3Compiler has not been initialized yet or has been closed");
        }
        return spoofax3Compiler;
    }

    public DynamicLoadingComponent getDynamicLoadingComponent() {
        if(dynamicLoadingComponent == null) {
            throw new RuntimeException("DynamicLoadingComponent has not been initialized yet or has been closed");
        }
        return dynamicLoadingComponent;
    }

    public PieComponent getPieComponent() {
        if(pieComponent == null) {
            throw new RuntimeException("PieComponent has not been initialized yet or has been closed");
        }
        return pieComponent;
    }

    public SpoofaxLwbComponent getSpoofaxLwbComponent() {
        if(spoofaxLwbComponent == null) {
            throw new RuntimeException("SpoofaxLwbComponent has not been initialized yet or has been closed");
        }
        return spoofaxLwbComponent;
    }


    @Override public ResourceRegistriesProvider getResourceRegistriesProvider(
        EclipseLoggerComponent loggerComponent
    ) {
        return new EmptyResourceRegistriesProvider();
    }

    @Override
    public TaskDefsProvider getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        this.resourceServiceComponent = resourceServiceComponent;
        return () -> {
            // Inside closure so that it is lazily initialized -> meta-language instances should be available.
            if(spoofax3Compiler == null) {
                spoofax3Compiler = new Spoofax3Compiler(
                    loggerComponent,
                    resourceServiceComponent,
                    platformComponent,
                    CfgLanguageFactory.getLanguage().getComponent(),
                    Sdf3LanguageFactory.getLanguage().getComponent(),
                    StrategoLanguageFactory.getLanguage().getComponent(),
                    EsvLanguageFactory.getLanguage().getComponent(),
                    StatixLanguageFactory.getLanguage().getComponent(),
                    LibSpoofax2LanguageFactory.getLanguage().getComponent(),
                    LibSpoofax2LanguageFactory.getLanguage().getResourcesComponent(),
                    LibStatixLanguageFactory.getLanguage().getComponent(),
                    LibStatixLanguageFactory.getLanguage().getResourcesComponent()
                );
            }
            if(dynamicLoadingComponent == null) {
                dynamicLoadingComponent = DaggerDynamicLoadingComponent.builder()
                    .dynamicLoadingPieModule(new DynamicLoadingPieModule(() -> new RootPieModule(PieBuilderImpl::new)))
                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .platformComponent(platformComponent)
                    .cfgComponent(CfgLanguageFactory.getLanguage().getComponent())
                    .spoofax3CompilerComponent(spoofax3Compiler.component)
                    .build();
            }
            if(spoofaxLwbComponent == null) {
                spoofaxLwbComponent = DaggerSpoofaxLwbComponent.builder()
                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .dynamicLoadingComponent(dynamicLoadingComponent)
                    .build();
            }
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.addAll(spoofax3Compiler.spoofaxCompilerComponent.getTaskDefs());
            taskDefs.addAll(spoofax3Compiler.component.getTaskDefs());
            taskDefs.addAll(dynamicLoadingComponent.getTaskDefs());
            return taskDefs;
        };
    }

    @Override public void customizePieModule(RootPieModule pieModule) {
        pieModule.withTracerFactory(LoggingTracer::new);
    }

    @Override
    public void start(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    ) {
        this.pieComponent = pieComponent;
        spoofaxLwbComponent.getDynamicChangeProcessor().register();
    }

    @Override public void close() {
        pieComponent = null;
        spoofaxLwbComponent.close();
        spoofaxLwbComponent = null;
        dynamicLoadingComponent.close();
        dynamicLoadingComponent = null;
        spoofax3Compiler.close();
        spoofax3Compiler = null;
        resourceServiceComponent = null;
    }
}
