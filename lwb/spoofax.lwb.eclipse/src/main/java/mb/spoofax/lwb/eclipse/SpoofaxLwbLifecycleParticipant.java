package mb.spoofax.lwb.eclipse;

import mb.cfg.eclipse.CfgLanguage;
import mb.esv.eclipse.EsvLanguage;
import mb.libspoofax2.eclipse.LibSpoofax2Language;
import mb.libstatix.eclipse.LibStatixLanguage;
import mb.pie.api.TaskDef;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.EmptyResourceRegistriesProvider;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.eclipse.Sdf3Language;
import mb.spoofax.eclipse.EclipseLifecycleParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import mb.spoofax.lwb.dynamicloading.DaggerDynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingModule;
import mb.statix.eclipse.StatixLanguage;
import mb.str.eclipse.StrategoLanguage;
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


    private @Nullable Spoofax3Compiler spoofax3Compiler;
    private @Nullable DynamicLoadingComponent dynamicLoadingComponent;
    private @Nullable PieComponent pieComponent;


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


    @Override public ResourceRegistriesProvider getResourceRegistriesProvider() {
        return new EmptyResourceRegistriesProvider();
    }

    @Override
    public TaskDefsProvider getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        return () -> {
            // Inside closure so that it is lazily initialized -> meta-language instances should be available.
            if(spoofax3Compiler == null) {
                spoofax3Compiler = new Spoofax3Compiler(
                    loggerComponent,
                    resourceServiceComponent,
                    platformComponent,
                    CfgLanguage.getInstance().getComponent(),
                    Sdf3Language.getInstance().getComponent(),
                    StrategoLanguage.getInstance().getComponent(),
                    EsvLanguage.getInstance().getComponent(),
                    StatixLanguage.getInstance().getComponent(),
                    LibSpoofax2Language.getInstance().getComponent(),
                    LibSpoofax2Language.getInstance().getResourcesComponent(),
                    LibStatixLanguage.getInstance().getComponent(),
                    LibStatixLanguage.getInstance().getResourcesComponent()
                );
            }
            if(dynamicLoadingComponent == null) {
                dynamicLoadingComponent = DaggerDynamicLoadingComponent.builder()
                    .dynamicLoadingModule(new DynamicLoadingModule(() -> new RootPieModule(PieBuilderImpl::new)))
                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .platformComponent(platformComponent)
                    .cfgComponent(CfgLanguage.getInstance().getComponent())
                    .spoofax3CompilerComponent(spoofax3Compiler.component)
                    .build();
            }
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.addAll(spoofax3Compiler.spoofaxCompilerComponent.getTaskDefs());
            taskDefs.addAll(spoofax3Compiler.component.getTaskDefs());
            taskDefs.addAll(dynamicLoadingComponent.getTaskDefs());
            return taskDefs;
        };
    }

    @Override
    public void start(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    ) {
        this.pieComponent = pieComponent;
    }

    @Override public void close() throws Exception {
        pieComponent = null;
        dynamicLoadingComponent.close();
        dynamicLoadingComponent = null;
        spoofax3Compiler.close();
        spoofax3Compiler = null;
    }
}
