package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgComponent;
import mb.esv.EsvComponent;
import mb.gpp.GppComponent;
import mb.gpp.GppResourcesComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.RootPieModule;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.EmptyParticipant;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibResourcesComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;

public class Spoofax3CompilerParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends EmptyParticipant<L, R, P> {
    private final Spoofax3CompilerModule spoofax3CompilerModule;
    private final Spoofax3CompilerJavaModule spoofax3CompilerJavaModule;

    public Spoofax3CompilerParticipant(
        Spoofax3CompilerModule spoofax3CompilerModule,
        Spoofax3CompilerJavaModule spoofax3CompilerJavaModule
    ) {
        this.spoofax3CompilerModule = spoofax3CompilerModule;
        this.spoofax3CompilerJavaModule = spoofax3CompilerJavaModule;
    }


    @Override
    public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "spoofax.lwb.compiler", new Version(0, 1, 0)); // TODO: get real version.
    }

    @Override
    public @Nullable String getGroup() {
        return "mb.spoofax.lwb";
    }


    @Override
    public @Nullable TaskDefsProvider getTaskDefsProvider(L loggerComponent, R baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent, P platformComponent) {
        return () -> {
            // Inside closure so that it is lazily initialized -> meta-language instances should be available.
            if(spoofax3Compiler == null) {
//                final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
//                final SpoofaxCompilerComponent spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
//                    .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
//                    .loggerComponent(loggerComponent)
//                    .resourceServiceComponent(resourceServiceComponent)
//                    .build();

                final CfgComponent cfgComponent = CfgLanguageFactory.getLanguage().getComponent();
                final Sdf3Component sdf3Component = Sdf3LanguageFactory.getLanguage().getComponent();
                final StrategoComponent strategoComponent = StrategoLanguageFactory.getLanguage().getComponent();
                final EsvComponent esvComponent = EsvLanguageFactory.getLanguage().getComponent();
                final StatixComponent statixComponent = StatixLanguageFactory.getLanguage().getComponent();

                final Sdf3ExtStatixComponent sdf3ExtStatixComponent = Sdf3ExtStatixLanguageFactory.getLanguage().getComponent();

                final StrategoLibComponent strategoLibComponent = StrategoLibLanguageFactory.getLanguage().getComponent();
                final StrategoLibResourcesComponent strategoLibResourcesComponent = StrategoLibLanguageFactory.getLanguage().getResourcesComponent();
                final GppComponent gppComponent = GppLanguageFactory.getLanguage().getComponent();
                final GppResourcesComponent gppResourcesComponent = GppLanguageFactory.getLanguage().getResourcesComponent();
                final LibSpoofax2Component libSpoofax2Component = LibSpoofax2LanguageFactory.getLanguage().getComponent();
                final LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent = LibSpoofax2LanguageFactory.getLanguage().getResourcesComponent();
                final LibStatixComponent libStatixComponent = LibStatixLanguageFactory.getLanguage().getComponent();
                final LibStatixResourcesComponent libStatixResourcesComponent = LibStatixLanguageFactory.getLanguage().getResourcesComponent();

                final Spoofax3CompilerComponent component = DaggerSpoofax3CompilerComponent.builder()
                    .spoofax3CompilerModule(spoofax3CompilerModule)
                    .spoofax3CompilerJavaModule(spoofax3CompilerJavaModule)

                    .loggerComponent(loggerComponent)
                    .resourceServiceComponent(resourceServiceComponent)

                    .cfgComponent(cfgComponent)
                    .sdf3Component(sdf3Component)
                    .strategoComponent(strategoComponent)
                    .esvComponent(esvComponent)
                    .statixComponent(statixComponent)

                    .sdf3ExtStatixComponent(sdf3ExtStatixComponent)

                    .strategoLibComponent(strategoLibComponent)
                    .strategoLibResourcesComponent(strategoLibResourcesComponent)
                    .gppComponent(gppComponent)
                    .gppResourcesComponent(gppResourcesComponent)

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
                    sdf3ExtStatixComponent,

                    strategoLibComponent,
                    strategoLibResourcesComponent,
                    gppComponent,
                    gppResourcesComponent,
                    libSpoofax2Component,
                    libSpoofax2ResourcesComponent,
                    libStatixComponent,
                    libStatixResourcesComponent,

                    spoofaxCompilerComponent,
                    component
                );
            }
//            if(dynamicLoadingComponent == null) {
//                dynamicLoadingComponent = DaggerEclipseDynamicLoadingComponent.builder()
//                    .dynamicLoadingPieModule(new DynamicLoadingPieModule(() -> new RootPieModule(PieBuilderImpl::new)))
//                    .loggerComponent(loggerComponent)
//                    .resourceServiceComponent(resourceServiceComponent)
//                    .platformComponent(platformComponent)
//                    .cfgComponent(CfgLanguageFactory.getLanguage().getComponent())
//                    .spoofax3CompilerComponent(spoofax3Compiler.component)
//                    .build();
//            }
//            SptLanguageFactory.getLanguage().getComponent().getLanguageUnderTestProviderWrapper().set(new DynamicLanguageUnderTestProvider(
//                SpoofaxLwbParticipant.getInstance().getDynamicLoadingComponent().getDynamicComponentManager(),
//                SpoofaxLwbParticipant.getInstance().getDynamicLoadingComponent().getDynamicLoad(),
//                spoofax3Compiler.component.getCompileLanguage(),
//                rootDirectory -> {
//                    // TODO: reduce code duplication with SpoofaxLwbBuilder
//                    return CompileLanguage.Args.builder()
//                        .rootDirectory(rootDirectory)
//                        .addJavaClassPathSuppliers(ClassPathUtil.getClassPathSupplier())
//                        .addJavaAnnotationProcessorPathSuppliers(ClassPathUtil.getClassPathSupplier())
//                        .build();
//                }
//            ));
//            if(spoofaxLwbComponent == null) {
//                spoofaxLwbComponent = DaggerSpoofaxLwbComponent.builder()
//                    .loggerComponent(loggerComponent)
//                    .resourceServiceComponent(resourceServiceComponent)
//                    .dynamicLoadingComponent(dynamicLoadingComponent)
//                    .build();
//            }
            final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
            taskDefs.addAll(spoofax3Compiler.spoofaxCompilerComponent.getTaskDefs());
            taskDefs.addAll(spoofax3Compiler.component.getTaskDefs());
            taskDefs.addAll(dynamicLoadingComponent.getTaskDefs());
            return taskDefs;
        };
    }
}
