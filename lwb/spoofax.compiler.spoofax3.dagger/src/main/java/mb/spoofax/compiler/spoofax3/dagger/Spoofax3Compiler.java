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
import mb.pie.api.PieBuilder;
import mb.sdf3.DaggerSdf3Component;
import mb.sdf3.DaggerSdf3ResourcesComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.Sdf3ResourcesComponent;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.DaggerResourceServiceComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.spoofax.core.platform.ResourceServiceModule;
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

public class Spoofax3Compiler {
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

    public Spoofax3Compiler(
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
        this.resourceServiceComponent = resourceServiceComponent;
        this.platformComponent = platformComponent;
        templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler, pieBuilderSupplier))
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        this.sdf3Component = sdf3Component;
        this.strategoComponent = strategoComponent;
        this.esvComponent = esvComponent;
        this.statixComponent = statixComponent;
        this.libSpoofax2Component = libSpoofax2Component;
        this.libSpoofax2ResourcesComponent = libSpoofax2ResourcesComponent;
        this.libStatixComponent = libStatixComponent;
        this.libStatixResourcesComponent = libStatixResourcesComponent;
        component = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
            .platformComponent(platformComponent)
            .sdf3Component(sdf3Component)
            .strategoComponent(strategoComponent)
            .esvComponent(esvComponent)
            .statixComponent(statixComponent)
            .libSpoofax2Component(libSpoofax2Component)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .libStatixComponent(libStatixComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .build();
    }

    public Spoofax3Compiler(
        ResourceServiceModule resourceServiceModule,
        LoggerFactoryModule loggerFactoryModule,
        PlatformPieModule platformPieModule
    ) {
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
            .build();
        platformComponent = DaggerPlatformComponent.builder()
            .resourceServiceComponent(resourceServiceComponent)
            .loggerFactoryModule(loggerFactoryModule)
            .platformPieModule(platformPieModule)
            .build();

        sdf3Component = DaggerSdf3Component.builder()
            .sdf3ResourcesComponent(sdf3ResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        strategoComponent = DaggerStrategoComponent.builder()
            .strategoResourcesComponent(strategoResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        esvComponent = DaggerEsvComponent.builder()
            .esvResourcesComponent(esvResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        statixComponent = DaggerStatixComponent.builder()
            .statixResourcesComponent(statixResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        libSpoofax2Component = DaggerLibSpoofax2Component.builder()
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        libStatixComponent = DaggerLibStatixComponent.builder()
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();

        templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler, platformComponent::newPieBuilder))
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        component = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .sdf3Component(sdf3Component)
            .strategoComponent(strategoComponent)
            .esvComponent(esvComponent)
            .statixComponent(statixComponent)
            .libSpoofax2Component(libSpoofax2Component)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .libStatixComponent(libStatixComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .build();
    }
}
