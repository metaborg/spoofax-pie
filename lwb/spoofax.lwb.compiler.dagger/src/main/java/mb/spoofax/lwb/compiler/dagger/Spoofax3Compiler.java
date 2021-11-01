package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgComponent;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.esv.EsvComponent;
import mb.esv.task.EsvConfig;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.StatelessSerializableFunction;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.esv.EsvConfigureException;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfig;
import mb.spoofax.lwb.compiler.stratego.StrategoConfigureException;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibResourcesComponent;

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

    public final Sdf3ExtStatixComponent sdf3ExtStatixComponent;

    public final StrategoLibComponent strategolibComponent;
    public final StrategoLibResourcesComponent strategolibResourcesComponent;
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

        Sdf3ExtStatixComponent sdf3ExtStatixComponent,

        StrategoLibComponent strategoLibComponent,
        StrategoLibResourcesComponent strategoLibResourcesComponent,
        LibSpoofax2Component libSpoofax2Component,
        LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent,
        LibStatixComponent libStatixComponent,
        LibStatixResourcesComponent libStatixResourcesComponent,

        TemplateCompiler templateCompiler,
        SpoofaxCompilerComponent spoofaxCompilerComponent,
        Spoofax3CompilerComponent component
    ) {
        this.loggerComponent = loggerComponent;
        this.resourceServiceComponent = resourceServiceComponent;
        this.platformComponent = platformComponent;

        this.cfgComponent = cfgComponent;
        this.sdf3Component = sdf3Component;
        this.strategoComponent = strategoComponent;
        this.esvComponent = esvComponent;
        this.statixComponent = statixComponent;
        this.sdf3ExtStatixComponent = sdf3ExtStatixComponent;
        this.strategolibComponent = strategoLibComponent;
        this.strategolibResourcesComponent = strategoLibResourcesComponent;
        this.libSpoofax2Component = libSpoofax2Component;
        this.libSpoofax2ResourcesComponent = libSpoofax2ResourcesComponent;
        this.libStatixComponent = libStatixComponent;
        this.libStatixResourcesComponent = libStatixResourcesComponent;

        this.templateCompiler = templateCompiler;
        this.spoofaxCompilerComponent = spoofaxCompilerComponent;
        this.component = component;

        this.sdf3Component.getSdf3SpecConfigFunctionWrapper().set(this.component.getConfigureSdf3().createFunction());
        this.esvComponent.getEsvConfigFunctionWrapper().set(this.component.getConfigureEsv().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxEsvConfig>, EsvConfigureException>, Result<Option<EsvConfig>, EsvConfigureException>>() {
                @Override
                public Result<Option<EsvConfig>, EsvConfigureException> apply(Result<Option<SpoofaxEsvConfig>, EsvConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxEsvConfig::getEsvConfig));
                }
            }));
        this.strategoComponent.getStrategoAnalyzeConfigFunctionWrapper().set(this.component.getConfigureStratego().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<StrategoCompileConfig>, StrategoConfigureException>, Result<Option<StrategoAnalyzeConfig>, StrategoConfigureException>>() {
                @Override
                public Result<Option<StrategoAnalyzeConfig>, StrategoConfigureException> apply(Result<Option<StrategoCompileConfig>, StrategoConfigureException> r) {
                    return r.map(o -> o.map(StrategoCompileConfig::toAnalyzeConfig));
                }
            }));
        this.statixComponent.getStatixConfigFunctionWrapper().set(this.component.getConfigureStatix().createFunction());
    }

    public static Spoofax3Compiler createDefault(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,

        CfgComponent cfgComponent,
        Sdf3Component sdf3Component,
        StrategoComponent strategoComponent,
        EsvComponent esvComponent,
        StatixComponent statixComponent,

        Sdf3ExtStatixComponent sdf3ExtStatixComponent,

        StrategoLibComponent strategoLibComponent,
        StrategoLibResourcesComponent strategoLibResourcesComponent,
        LibSpoofax2Component libSpoofax2Component,
        LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent,
        LibStatixComponent libStatixComponent,
        LibStatixResourcesComponent libStatixResourcesComponent
    ) {
        final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        final SpoofaxCompilerComponent spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        final Spoofax3CompilerComponent component = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
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
            .libSpoofax2Component(libSpoofax2Component)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .libStatixComponent(libStatixComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .build();
        return new Spoofax3Compiler(
            loggerComponent,
            resourceServiceComponent,
            platformComponent,

            cfgComponent,
            sdf3Component,
            strategoComponent,
            esvComponent,
            statixComponent,
            sdf3ExtStatixComponent,
            strategoLibComponent, strategoLibResourcesComponent, libSpoofax2Component,
            libSpoofax2ResourcesComponent,
            libStatixComponent,
            libStatixResourcesComponent,

            templateCompiler,
            spoofaxCompilerComponent,
            component
        );
    }

    @Override public void close() {
        platformComponent.close();
        resourceServiceComponent.close();
    }
}
