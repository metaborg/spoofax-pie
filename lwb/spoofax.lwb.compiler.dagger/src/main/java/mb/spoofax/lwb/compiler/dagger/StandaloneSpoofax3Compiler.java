package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgComponent;
import mb.cfg.CfgResourcesComponent;
import mb.cfg.DaggerCfgComponent;
import mb.cfg.DaggerCfgResourcesComponent;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.result.Result;
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
import mb.pie.api.Function;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.resource.hierarchical.ResourcePath;
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

public class StandaloneSpoofax3Compiler implements AutoCloseable {
    public final Spoofax3Compiler compiler;
    public final PieComponent pieComponent;

    /**
     * Creates a {@link StandaloneSpoofax3Compiler} by:
     * <ul>
     *     <li>Creating all meta-language and library resource components and composing those into a {@link ResourceServiceModule} using {@code resourceServiceModule}</li>
     *     <li>Creating all meta-language and library components, and all compiler components, and composing their task definitions into a {@link PieComponent} using {@code pieModule}</li>
     * </ul>
     * This facade is useful for tests or other usages where we run the compiler in an standalone/isolated way.
     *
     * @param loggerComponent       {@link LoggerComponent} to pass as a dependency to components that require it.
     * @param resourceServiceModule {@link ResourceServiceModule} used to compose into a {@link ResourceServiceModule}.
     * @param pieModule             {@link PieModule} used to compose into a {@link PieComponent}.
     */
    public StandaloneSpoofax3Compiler(
        LoggerComponent loggerComponent,
        ResourceServiceModule resourceServiceModule,
        PieModule pieModule
    ) {
        final CfgResourcesComponent cfgResourcesComponent = DaggerCfgResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(cfgResourcesComponent);
        final Sdf3ResourcesComponent sdf3ResourcesComponent = DaggerSdf3ResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(sdf3ResourcesComponent);
        final StrategoResourcesComponent strategoResourcesComponent = DaggerStrategoResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(strategoResourcesComponent);
        final EsvResourcesComponent esvResourcesComponent = DaggerEsvResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(esvResourcesComponent);
        final StatixResourcesComponent statixResourcesComponent = DaggerStatixResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(statixResourcesComponent);
        final LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent = DaggerLibSpoofax2ResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(libSpoofax2ResourcesComponent);
        final LibStatixResourcesComponent libStatixResourcesComponent = DaggerLibStatixResourcesComponent.create();
        resourceServiceModule.addRegistriesFrom(libStatixResourcesComponent);

        final ResourceServiceComponent resourceServiceComponent = DaggerResourceServiceComponent.builder()
            .resourceServiceModule(resourceServiceModule)
            .loggerComponent(loggerComponent)
            .build();
        final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();

        final CfgComponent cfgComponent = DaggerCfgComponent.builder()
            .loggerComponent(loggerComponent)
            .cfgResourcesComponent(cfgResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        final Function<ResourcePath, Result<CfgToObject.Output, CfgRootDirectoryToObjectException>> cfgFunction =
            cfgComponent.getCfgRootDirectoryToObject().createFunction();
        pieModule.addTaskDefsFrom(cfgComponent);
        final Sdf3Component sdf3Component = DaggerSdf3Component.builder()
            .loggerComponent(loggerComponent)
            .sdf3ResourcesComponent(sdf3ResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(sdf3Component);
        final StrategoComponent strategoComponent = DaggerStrategoComponent.builder()
            .loggerComponent(loggerComponent)
            .strategoResourcesComponent(strategoResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(strategoComponent);
        final EsvComponent esvComponent = DaggerEsvComponent.builder()
            .loggerComponent(loggerComponent)
            .esvResourcesComponent(esvResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(esvComponent);
        final StatixComponent statixComponent = DaggerStatixComponent.builder()
            .loggerComponent(loggerComponent)
            .statixResourcesComponent(statixResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(statixComponent);
        final LibSpoofax2Component libSpoofax2Component = DaggerLibSpoofax2Component.builder()
            .loggerComponent(loggerComponent)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(libSpoofax2Component);
        final LibStatixComponent libStatixComponent = DaggerLibStatixComponent.builder()
            .loggerComponent(loggerComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        pieModule.addTaskDefsFrom(libStatixComponent);

        final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        final SpoofaxCompilerComponent spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        pieModule.addTaskDefsFrom(spoofaxCompilerComponent);
        final Spoofax3CompilerComponent component = DaggerSpoofax3CompilerComponent.builder()
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
        pieModule.addTaskDefsFrom(component);

        this.compiler = Spoofax3Compiler.createDefault(
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
            libStatixResourcesComponent
        );
        this.pieComponent = DaggerPieComponent.builder()
            .pieModule(pieModule)
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
    }

    @Override public void close() {
        pieComponent.close();
    }
}
