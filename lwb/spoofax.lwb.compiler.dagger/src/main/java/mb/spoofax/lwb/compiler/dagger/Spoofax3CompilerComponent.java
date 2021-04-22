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
import mb.spoofax.lwb.compiler.CheckLanguageSpecification;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageSpecification;
import mb.spoofax.lwb.compiler.esv.ConfigureEsv;
import mb.spoofax.lwb.compiler.sdf3.ConfigureSdf3;
import mb.spoofax.lwb.compiler.statix.ConfigureStatix;
import mb.spoofax.lwb.compiler.stratego.ConfigureStratego;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;

import java.util.Set;

@Spoofax3CompilerScope
@Component(
    modules = {
        Spoofax3CompilerModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        CfgComponent.class,
        Sdf3Component.class,
        StrategoComponent.class,
        EsvComponent.class,
        StatixComponent.class,
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


    ConfigureEsv getConfigureEsv();

    ConfigureSdf3 getConfigureSdf3();

    ConfigureStatix getConfigureStatix();

    ConfigureStratego getConfigureStratego();


    @Override @Spoofax3CompilerQualifier Set<TaskDef<?, ?>> getTaskDefs();
}