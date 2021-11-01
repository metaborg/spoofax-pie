package mb.spoofax.lwb.compiler.dagger;

import dagger.Component;
import mb.cfg.CfgComponent;
import mb.esv.EsvComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.spoofax.lwb.compiler.CheckLanguageSpecification;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageSpecification;
import mb.spoofax.lwb.compiler.cfg.SpoofaxCfgCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfigure;
import mb.spoofax.lwb.compiler.generator.LanguageProjectGenerator;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Check;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Configure;
import mb.spoofax.lwb.compiler.statix.CheckStatix;
import mb.spoofax.lwb.compiler.statix.ConfigureStatix;
import mb.spoofax.lwb.compiler.stratego.CheckStratego;
import mb.spoofax.lwb.compiler.stratego.ConfigureStratego;
import mb.spoofax.lwb.compiler.stratego.StrategoLibUtil;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibResourcesComponent;

import java.util.Set;

@Spoofax3CompilerScope
@Component(
    modules = {
        Spoofax3CompilerModule.class,
        Spoofax3CompilerJavaModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        CfgComponent.class,
        Sdf3Component.class,
        StrategoComponent.class,
        EsvComponent.class,
        StatixComponent.class,

        Sdf3ExtStatixComponent.class,

        StrategoLibComponent.class,
        StrategoLibResourcesComponent.class,
        LibSpoofax2Component.class,
        LibSpoofax2ResourcesComponent.class,
        LibStatixComponent.class,
        LibStatixResourcesComponent.class
    }
)
public interface Spoofax3CompilerComponent extends TaskDefsProvider {
    CompileLanguage getCompileLanguage();

    CheckLanguageSpecification getCheckLanguageSpecification();

    CompileLanguageSpecification getCompileLanguageSpecification();


    SpoofaxCfgCheck getSpoofaxCfgCheck();

    SpoofaxEsvConfigure getSpoofaxEsvConfigure();

    SpoofaxEsvCheck getSpoofaxEsvCheck();

    SpoofaxSdf3Configure getSpoofaxSdf3Configure();

    SpoofaxSdf3Check getSpoofaxSdf3Check();

    CheckStatix getCheckStatix();

    ConfigureStatix getConfigureStatix();

    CheckStratego getCheckStratego();

    ConfigureStratego getConfigureStratego();

    StrategoLibUtil getStrategoLibUtil();


    LanguageProjectGenerator getLanguageProjectGenerator();


    @Override @Spoofax3CompilerQualifier Set<TaskDef<?, ?>> getTaskDefs();
}
