package mb.spoofax.compiler.spoofax3.dagger;

import mb.esv.DaggerEsvComponent;
import mb.esv.DaggerEsvResourcesComponent;
import mb.esv.EsvComponent;
import mb.esv.EsvResourcesComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libspoofax2.DaggerLibSpoofax2ResourcesComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.DaggerLibStatixComponent;
import mb.libstatix.DaggerLibStatixResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.sdf3.DaggerSdf3Component;
import mb.sdf3.DaggerSdf3ResourcesComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.Sdf3ResourcesComponent;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.DaggerStatixComponent;
import mb.statix.DaggerStatixResourcesComponent;
import mb.statix.StatixComponent;
import mb.statix.StatixResourcesComponent;
import mb.str.DaggerStrategoComponent;
import mb.str.DaggerStrategoResourcesComponent;
import mb.str.StrategoComponent;
import mb.str.StrategoResourcesComponent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class Spoofax3Compiler implements AutoCloseable {
    public final LoggerComponent loggerComponent;
    public final ResourceServiceComponent resourceServiceComponent;
    public final PlatformComponent platformComponent;

    public final TemplateCompiler templateCompiler;

    public final SpoofaxCompilerComponent spoofaxCompilerComponent;

    public final Sdf3Component sdf3Component;
    public final StrategoComponent strategoComponent;
    public final EsvComponent esvComponent;
    public final StatixComponent statixComponent;
    public final LibSpoofax2Component libSpoofax2Component;
    public final LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent;
    public final LibStatixComponent libStatixComponent;
    public final LibStatixResourcesComponent libStatixResourcesComponent;

    public final Spoofax3CompilerComponent component;

    public final PieComponent pieComponent;

    public Spoofax3Compiler(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier,
        Sdf3Component sdf3Component,
        StrategoComponent strategoComponent,
        EsvComponent esvComponent,
        StatixComponent statixComponent,
        LibSpoofax2Component libSpoofax2Component,
        LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent,
        LibStatixComponent libStatixComponent,
        LibStatixResourcesComponent libStatixResourcesComponent
    ) {
        this.loggerComponent = loggerComponent;
        this.resourceServiceComponent = resourceServiceComponent;
        this.platformComponent = platformComponent;
        templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        final RootPieModule pieModule = new RootPieModule(pieBuilderSupplier);
        spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        pieModule.addTaskDefsFrom(spoofaxCompilerComponent);
        this.sdf3Component = sdf3Component;
        pieModule.addTaskDefsFrom(sdf3Component);
        this.strategoComponent = strategoComponent;
        pieModule.addTaskDefsFrom(strategoComponent);
        this.esvComponent = esvComponent;
        pieModule.addTaskDefsFrom(esvComponent);
        this.statixComponent = statixComponent;
        pieModule.addTaskDefsFrom(statixComponent);
        this.libSpoofax2Component = libSpoofax2Component;
        pieModule.addTaskDefsFrom(libSpoofax2Component);
        this.libSpoofax2ResourcesComponent = libSpoofax2ResourcesComponent;
        this.libStatixComponent = libStatixComponent;
        pieModule.addTaskDefsFrom(libStatixComponent);
        this.libStatixResourcesComponent = libStatixResourcesComponent;
        component = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .sdf3Component(sdf3Component)
            .strategoComponent(strategoComponent)
            .esvComponent(esvComponent)
            .statixComponent(statixComponent)
            .libSpoofax2Component(libSpoofax2Component)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .libStatixComponent(libStatixComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .build();
        pieModule.addTaskDefsFrom(component);
        pieComponent = DaggerRootPieComponent.builder()
            .rootPieModule(pieModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }

    public Spoofax3Compiler(
        LoggerComponent loggerComponent,
        ResourceServiceModule resourceServiceModule,
        PieModule pieModule
    ) {
        this.loggerComponent = loggerComponent;
        final Sdf3ResourcesComponent sdf3ResourcesComponent = DaggerSdf3ResourcesComponent.create();
        sdf3ResourcesComponent.addResourceRegistriesTo(resourceServiceModule);
        final StrategoResourcesComponent strategoResourcesComponent = DaggerStrategoResourcesComponent.create();
        strategoResourcesComponent.addResourceRegistriesTo(resourceServiceModule);
        final EsvResourcesComponent esvResourcesComponent = DaggerEsvResourcesComponent.create();
        esvResourcesComponent.addResourceRegistriesTo(resourceServiceModule);
        final StatixResourcesComponent statixResourcesComponent = DaggerStatixResourcesComponent.create();
        statixResourcesComponent.addResourceRegistriesTo(resourceServiceModule);
        libSpoofax2ResourcesComponent = DaggerLibSpoofax2ResourcesComponent.create();
        libSpoofax2ResourcesComponent.addResourceRegistriesTo(resourceServiceModule);
        libStatixResourcesComponent = DaggerLibStatixResourcesComponent.create();
        libStatixResourcesComponent.addResourceRegistriesTo(resourceServiceModule);
        resourceServiceComponent = DaggerResourceServiceComponent.builder()
            .resourceServiceModule(resourceServiceModule)
            .loggerComponent(loggerComponent)
            .build();
        platformComponent = DaggerPlatformComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();

        sdf3Component = DaggerSdf3Component.builder()
            .loggerComponent(loggerComponent)
            .sdf3ResourcesComponent(sdf3ResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(sdf3Component);
        strategoComponent = DaggerStrategoComponent.builder()
            .loggerComponent(loggerComponent)
            .strategoResourcesComponent(strategoResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(strategoComponent);
        esvComponent = DaggerEsvComponent.builder()
            .loggerComponent(loggerComponent)
            .esvResourcesComponent(esvResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(esvComponent);
        statixComponent = DaggerStatixComponent.builder()
            .loggerComponent(loggerComponent)
            .statixResourcesComponent(statixResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(statixComponent);
        libSpoofax2Component = DaggerLibSpoofax2Component.builder()
            .loggerComponent(loggerComponent)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(libSpoofax2Component);
        libStatixComponent = DaggerLibStatixComponent.builder()
            .loggerComponent(loggerComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(libStatixComponent);

        templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        pieModule.addTaskDefsFrom(spoofaxCompilerComponent);
        component = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .sdf3Component(sdf3Component)
            .strategoComponent(strategoComponent)
            .esvComponent(esvComponent)
            .statixComponent(statixComponent)
            .libSpoofax2Component(libSpoofax2Component)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .libStatixComponent(libStatixComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .build();
        pieModule.addTaskDefsFrom(component);

        pieComponent = DaggerPieComponent.builder()
            .pieModule(pieModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }

    @Override public void close() {
        pieComponent.close();
    }
}
