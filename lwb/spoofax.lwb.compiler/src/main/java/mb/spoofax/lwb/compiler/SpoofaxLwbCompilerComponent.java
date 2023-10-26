package mb.spoofax.lwb.compiler;

import dagger.Component;
import mb.cfg.CfgComponent;
import mb.dynamix.DynamixComponent;
import mb.esv.EsvComponent;
import mb.gpp.GppComponent;
import mb.gpp.GppResourcesComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.llvm.LLVMComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3_ext_dynamix.Sdf3ExtDynamixComponent;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.spoofax.lwb.compiler.cfg.SpoofaxCfgCheck;
import mb.spoofax.lwb.compiler.definition.CheckLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileMetaLanguageSources;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixCheck;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixConfigure;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfigure;
import mb.spoofax.lwb.compiler.generator.LanguageProjectGenerator;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Check;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Configure;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCheck;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixConfigure;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoCheck;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoConfigure;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibResourcesComponent;
import mb.tim.TimComponent;

import java.util.Set;

@SpoofaxLwbCompilerScope
@Component(
    modules = {
        SpoofaxLwbCompilerModule.class,
        SpoofaxLwbCompilerJavaModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,

        CfgComponent.class,
        Sdf3Component.class,
        StrategoComponent.class,
        EsvComponent.class,
        StatixComponent.class,
        TimComponent.class,
        DynamixComponent.class,
        LLVMComponent.class,

        Sdf3ExtStatixComponent.class,
        Sdf3ExtDynamixComponent.class,

        StrategoLibComponent.class,
        StrategoLibResourcesComponent.class,
        GppComponent.class,
        GppResourcesComponent.class,

        LibSpoofax2Component.class,
        LibSpoofax2ResourcesComponent.class,
        LibStatixComponent.class,
        LibStatixResourcesComponent.class,
    }
)
public interface SpoofaxLwbCompilerComponent extends TaskDefsProvider, AutoCloseable {
    SpoofaxLwbCompilerComponentManagerWrapper getSpoofaxLwbCompilerComponentManagerWrapper();

    LoggerComponent getLoggerComponent();

    ResourceServiceComponent getResourceServiceComponent();


    CfgComponent getCfgComponent();

    Sdf3Component getSdf3Component();

    StrategoComponent getStrategoComponent();

    EsvComponent getEsvComponent();

    StatixComponent getStatixComponent();

    TimComponent getTimComponent();

    DynamixComponent getDynamixComponent();

    LLVMComponent getLLVMComponent();

    Sdf3ExtStatixComponent getSdf3ExtStatixComponent();

    Sdf3ExtDynamixComponent getSdf3ExtDynamixComponent();


    StrategoLibComponent getStrategoLibComponent();

    StrategoLibResourcesComponent getStrategoLibResourcesComponent();

    GppComponent getGppComponent();

    GppResourcesComponent getGppResourcesComponent();


    LibSpoofax2Component getLibSpoofax2Component();

    LibSpoofax2ResourcesComponent getLibSpoofax2ResourcesComponent();

    LibStatixComponent getLibStatixComponent();

    LibStatixResourcesComponent getLibStatixResourcesComponent();


    CompileLanguageDefinition getCompileLanguageDefinition();

    CheckLanguageDefinition getCheckLanguageDefinition();

    CompileMetaLanguageSources getCompileMetaLanguageSources();


    SpoofaxCfgCheck getSpoofaxCfgCheck();

    SpoofaxEsvConfigure getSpoofaxEsvConfigure();

    SpoofaxEsvCheck getSpoofaxEsvCheck();

    SpoofaxSdf3Configure getSpoofaxSdf3Configure();

    SpoofaxSdf3Check getSpoofaxSdf3Check();

    SpoofaxStatixCheck getSpoofaxStatixCheck();

    SpoofaxStatixConfigure getSpoofaxStatixConfigure();

    SpoofaxDynamixCheck getSpoofaxDynamixCheck();

    SpoofaxDynamixConfigure getSpoofaxDynamixConfigure();

    SpoofaxStrategoCheck getSpoofaxStrategoCheck();

    SpoofaxStrategoConfigure getSpoofaxStrategoConfigure();


    LanguageProjectGenerator getLanguageProjectGenerator();


    @Override @SpoofaxLwbCompilerQualifier Set<TaskDef<?, ?>> getTaskDefs();

    @Override default void close() {
        // For now, nothing to close. Spoofax3CompilerComponent may implement AutoCloseable in the future.
    }
}
