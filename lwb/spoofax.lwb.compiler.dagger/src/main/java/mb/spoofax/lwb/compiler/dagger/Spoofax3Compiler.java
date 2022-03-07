package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgComponent;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.dynamix.DynamixComponent;
import mb.dynamix.task.DynamixConfig;
import mb.esv.EsvComponent;
import mb.esv.task.EsvConfig;
import mb.gpp.GppComponent;
import mb.gpp.GppResourcesComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.StatelessSerializableFunction;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3_ext_dynamix.Sdf3ExtDynamixComponent;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixConfig;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixConfigureException;
import mb.spoofax.lwb.compiler.esv.EsvConfigureException;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfig;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Config;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixConfig;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixConfigureException;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoConfigureException;
import mb.statix.StatixComponent;
import mb.statix.task.StatixConfig;
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
    public final DynamixComponent dynamixComponent;

    public final Sdf3ExtStatixComponent sdf3ExtStatixComponent;
    public final Sdf3ExtDynamixComponent sdf3ExtDynamixComponent;

    public final StrategoLibComponent strategolibComponent;
    public final StrategoLibResourcesComponent strategolibResourcesComponent;
    public final GppComponent gppComponent;
    public final GppResourcesComponent gppResourcesComponent;
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
        DynamixComponent dynamixComponent,

        Sdf3ExtStatixComponent sdf3ExtStatixComponent,
        Sdf3ExtDynamixComponent sdf3ExtDynamixComponent,

        StrategoLibComponent strategoLibComponent,
        StrategoLibResourcesComponent strategoLibResourcesComponent,
        GppComponent gppComponent,
        GppResourcesComponent gppResourcesComponent,
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
        this.dynamixComponent = dynamixComponent;
        this.sdf3ExtStatixComponent = sdf3ExtStatixComponent;
        this.sdf3ExtDynamixComponent = sdf3ExtDynamixComponent;
        this.strategolibComponent = strategoLibComponent;
        this.strategolibResourcesComponent = strategoLibResourcesComponent;
        this.gppComponent = gppComponent;
        this.gppResourcesComponent = gppResourcesComponent;
        this.libSpoofax2Component = libSpoofax2Component;
        this.libSpoofax2ResourcesComponent = libSpoofax2ResourcesComponent;
        this.libStatixComponent = libStatixComponent;
        this.libStatixResourcesComponent = libStatixResourcesComponent;

        this.templateCompiler = templateCompiler;
        this.spoofaxCompilerComponent = spoofaxCompilerComponent;
        this.component = component;

        this.sdf3Component.getSdf3SpecConfigFunctionWrapper().set(this.component.getSpoofaxSdf3Configure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException>, Result<Option<Sdf3SpecConfig>, SpoofaxSdf3ConfigureException>>() {
                @Override
                public Result<Option<Sdf3SpecConfig>, SpoofaxSdf3ConfigureException> apply(Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxSdf3Config::getSdf3SpecConfig));
                }
            }
        ));
        this.esvComponent.getEsvConfigFunctionWrapper().set(this.component.getSpoofaxEsvConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxEsvConfig>, EsvConfigureException>, Result<Option<EsvConfig>, EsvConfigureException>>() {
                @Override
                public Result<Option<EsvConfig>, EsvConfigureException> apply(Result<Option<SpoofaxEsvConfig>, EsvConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxEsvConfig::getEsvConfig));
                }
            }));
        this.strategoComponent.getStrategoAnalyzeConfigFunctionWrapper().set(this.component.getSpoofaxStrategoConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<StrategoCompileConfig>, SpoofaxStrategoConfigureException>, Result<Option<StrategoAnalyzeConfig>, SpoofaxStrategoConfigureException>>() {
                @Override
                public Result<Option<StrategoAnalyzeConfig>, SpoofaxStrategoConfigureException> apply(Result<Option<StrategoCompileConfig>, SpoofaxStrategoConfigureException> r) {
                    return r.map(o -> o.map(StrategoCompileConfig::toAnalyzeConfig));
                }
            }));
        this.statixComponent.getStatixConfigFunctionWrapper().set(this.component.getSpoofaxStatixConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException>, Result<Option<StatixConfig>, SpoofaxStatixConfigureException>>() {
                @Override
                public Result<Option<StatixConfig>, SpoofaxStatixConfigureException> apply(Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxStatixConfig::getStatixConfig));
                }
            }));
        this.dynamixComponent.getDynamixConfigFunctionWrapper().set(this.component.getSpoofaxDynamixConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxDynamixConfig>, SpoofaxDynamixConfigureException>, Result<Option<DynamixConfig>, SpoofaxDynamixConfigureException>>() {
                @Override
                public Result<Option<DynamixConfig>, SpoofaxDynamixConfigureException> apply(Result<Option<SpoofaxDynamixConfig>, SpoofaxDynamixConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxDynamixConfig::getDynamixConfig));
                }
            }));
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
        DynamixComponent dynamixComponent,

        Sdf3ExtStatixComponent sdf3ExtStatixComponent,
        Sdf3ExtDynamixComponent sdf3ExtDynamixComponent,

        StrategoLibComponent strategoLibComponent,
        StrategoLibResourcesComponent strategoLibResourcesComponent,
        GppComponent gppComponent,
        GppResourcesComponent gppResourcesComponent,
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
            .dynamixComponent(dynamixComponent)

            .sdf3ExtStatixComponent(sdf3ExtStatixComponent)
            .sdf3ExtDynamixComponent(sdf3ExtDynamixComponent)

            .strategoLibComponent(strategoLibComponent)
            .strategoLibResourcesComponent(strategoLibResourcesComponent)
            .gppComponent(gppComponent)
            .gppResourcesComponent(gppResourcesComponent)
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
            dynamixComponent,

            sdf3ExtStatixComponent,
            sdf3ExtDynamixComponent,

            strategoLibComponent,
            strategoLibResourcesComponent,
            gppComponent,
            gppResourcesComponent,
            libSpoofax2Component,
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
