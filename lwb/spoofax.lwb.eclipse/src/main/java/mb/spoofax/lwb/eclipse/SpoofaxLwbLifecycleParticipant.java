package mb.spoofax.lwb.eclipse;

import mb.cfg.CfgComponent;
import mb.cfg.eclipse.CfgLanguageFactory;
import mb.esv.eclipse.EsvEclipseComponent;
import mb.esv.eclipse.EsvLanguageFactory;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libspoofax2.eclipse.LibSpoofax2EclipseComponent;
import mb.libspoofax2.eclipse.LibSpoofax2LanguageFactory;
import mb.libstatix.LibStatixResourcesComponent;
import mb.libstatix.eclipse.LibStatixEclipseComponent;
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
import mb.sdf3.Sdf3Component;
import mb.sdf3.eclipse.Sdf3LanguageFactory;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipseLifecycleParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import mb.spoofax.lwb.compiler.dagger.Spoofax3CompilerModule;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingPieModule;
import mb.spoofax.lwb.eclipse.compiler.DaggerEclipseSpoofax3CompilerComponent;
import mb.spoofax.lwb.eclipse.compiler.EclipseSpoofax3CompilerComponent;
import mb.spoofax.lwb.eclipse.dynamicloading.DaggerEclipseDynamicLoadingComponent;
import mb.spoofax.lwb.eclipse.dynamicloading.EclipseDynamicLoadingComponent;
import mb.spoofax.lwb.eclipse.util.ClassPathUtil;
import mb.spt.dynamicloading.CompileAndLoadLanguageUnderTestProvider;
import mb.spt.eclipse.SptLanguageFactory;
import mb.statix.eclipse.StatixEclipseComponent;
import mb.statix.eclipse.StatixLanguageFactory;
import mb.str.eclipse.StrategoEclipseComponent;
import mb.str.eclipse.StrategoLanguageFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

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
    private @Nullable EclipseDynamicLoadingComponent dynamicLoadingComponent;
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

    public EclipseDynamicLoadingComponent getDynamicLoadingComponent() {
        if(dynamicLoadingComponent == null) {
            throw new RuntimeException("EclipseDynamicLoadingComponent has not been initialized yet or has been closed");
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
                final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
                final SpoofaxCompilerComponent spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
                    .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .build();

                final CfgComponent cfgComponent = CfgLanguageFactory.getLanguage().getComponent();
                final Sdf3Component sdf3Component = Sdf3LanguageFactory.getLanguage().getComponent();
                final StrategoEclipseComponent strategoComponent = StrategoLanguageFactory.getLanguage().getComponent();
                final EsvEclipseComponent esvComponent = EsvLanguageFactory.getLanguage().getComponent();
                final StatixEclipseComponent statixComponent = StatixLanguageFactory.getLanguage().getComponent();
                final LibSpoofax2EclipseComponent libSpoofax2Component = LibSpoofax2LanguageFactory.getLanguage().getComponent();
                final LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent = LibSpoofax2LanguageFactory.getLanguage().getResourcesComponent();
                final LibStatixEclipseComponent libStatixComponent = LibStatixLanguageFactory.getLanguage().getComponent();
                final LibStatixResourcesComponent libStatixResourcesComponent = LibStatixLanguageFactory.getLanguage().getResourcesComponent();

                final EclipseSpoofax3CompilerComponent component = DaggerEclipseSpoofax3CompilerComponent.builder()
                    .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .cfgComponent(cfgComponent)
                    .sdf3Component(sdf3Component)
                    .strategoComponent(strategoComponent)
                    .esvComponent(esvComponent)
                    .statixComponent(statixComponent)
                    .libSpoofax2Component(libSpoofax2Component)
                    .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
                    .libStatixComponent(libStatixComponent)
                    .libStatixResourcesComponent(libStatixResourcesComponent)
                    .build();

                spoofax3Compiler = new Spoofax3Compiler(
                    loggerComponent,
                    resourceServiceComponent,
                    platformComponent,

                    cfgComponent,
                    sdf3Component,
                    strategoComponent,
                    esvComponent,
                    statixComponent,
                    libSpoofax2Component,
                    libSpoofax2ResourcesComponent,
                    libStatixComponent,
                    libStatixResourcesComponent,

                    templateCompiler,
                    spoofaxCompilerComponent,
                    component
                );
            }
            if(dynamicLoadingComponent == null) {
                dynamicLoadingComponent = DaggerEclipseDynamicLoadingComponent.builder()
                    .dynamicLoadingPieModule(new DynamicLoadingPieModule(() -> new RootPieModule(PieBuilderImpl::new)))
                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .platformComponent(platformComponent)
                    .cfgComponent(CfgLanguageFactory.getLanguage().getComponent())
                    .spoofax3CompilerComponent(spoofax3Compiler.component)
                    .build();
            }
            SptLanguageFactory.getLanguage().getComponent().getLanguageUnderTestProviderWrapper().set(new CompileAndLoadLanguageUnderTestProvider(
                SpoofaxLwbLifecycleParticipant.getInstance().getDynamicLoadingComponent().getDynamicLoad(),
                rootDirectory -> {
                    // TODO: reduce code completion with SpoofaxLwbBuilder
                    final List<File> classPath = ClassPathUtil.getClassPath();
                    return CompileLanguage.Args.builder()
                        .rootDirectory(rootDirectory)
                        .additionalJavaClassPath(classPath)
                        .additionalJavaAnnotationProcessorPath(classPath)
                        .build();
                }
            ));
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

    @Override public @Nullable EclipseLanguageComponent getLanguageComponent(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        return null;
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
        spoofaxLwbComponent.getDynamicEditorTracker().register();
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
