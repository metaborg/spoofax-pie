package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgComponent;
import mb.esv.EsvComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;

import java.nio.charset.StandardCharsets;

public class Spoofax3Compiler implements AutoCloseable {
    public final LoggerComponent loggerComponent;
    public final ResourceServiceComponent resourceServiceComponent;
    public final PlatformComponent platformComponent;

    public final CfgComponent cfgComponent;
    public final Sdf3Component sdf3Component;
    public final StrategoComponent strategoComponent;
    public final EsvComponent esvComponent;
    public final StatixComponent statixComponent;
    public final LibSpoofax2Component libSpoofax2Component;
    public final LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent;
    public final LibStatixComponent libStatixComponent;
    public final LibStatixResourcesComponent libStatixResourcesComponent;

    public final TemplateCompiler templateCompiler;
    public final SpoofaxCompilerComponent spoofaxCompilerComponent;
    public final Spoofax3CompilerComponent component;

    public Spoofax3Compiler(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,

        CfgComponent cfgComponent,
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

        this.cfgComponent = cfgComponent;
        this.sdf3Component = sdf3Component;
        this.strategoComponent = strategoComponent;
        this.esvComponent = esvComponent;
        this.statixComponent = statixComponent;
        this.libSpoofax2Component = libSpoofax2Component;
        this.libSpoofax2ResourcesComponent = libSpoofax2ResourcesComponent;
        this.libStatixComponent = libStatixComponent;
        this.libStatixResourcesComponent = libStatixResourcesComponent;

        this.templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        this.spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        this.component = DaggerSpoofax3CompilerComponent.builder()
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

        // Inject ESV config function.
        this.esvComponent.getEsvConfigFunctionWrapper().set(this.component.getConfigureEsv().createFunction());
    }

    @Override public void close() throws Exception {
        platformComponent.close();
        resourceServiceComponent.close();
    }
}
