package mb.spoofax.lwb.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.pie.task.java.CompileJava;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.lwb.compiler.CheckLanguageSpecification;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageSpecification;
import mb.spoofax.lwb.compiler.cfg.SpoofaxCfgCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCompile;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfigure;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Check;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Compile;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Configure;
import mb.spoofax.lwb.compiler.statix.CheckStatix;
import mb.spoofax.lwb.compiler.statix.CompileStatix;
import mb.spoofax.lwb.compiler.statix.ConfigureStatix;
import mb.spoofax.lwb.compiler.stratego.CheckStratego;
import mb.spoofax.lwb.compiler.stratego.CompileStratego;
import mb.spoofax.lwb.compiler.stratego.ConfigureStratego;

import java.util.HashSet;
import java.util.Set;

@Module
public class Spoofax3CompilerModule {
    private final TemplateCompiler templateCompiler;

    public Spoofax3CompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }


    @Provides @Spoofax3CompilerScope
    TemplateCompiler provideTemplateCompiler() {
        return templateCompiler;
    }


    @Provides @Spoofax3CompilerScope
    static UnarchiveFromJar provideUnarchiveFromJar() {
        return new UnarchiveFromJar();
    }


    @Provides @Spoofax3CompilerQualifier @Spoofax3CompilerScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        CompileLanguage compileLanguage,
        CheckLanguageSpecification checkLanguageSpecification,
        CompileLanguageSpecification compileLanguageSpecification,

        SpoofaxCfgCheck spoofaxCfgCheck,

        SpoofaxSdf3Configure spoofaxSdf3Configure,
        SpoofaxSdf3Check spoofaxSdf3Check,
        SpoofaxSdf3Compile spoofaxSdf3Compile,

        SpoofaxEsvConfigure spoofaxEsvConfigure,
        SpoofaxEsvCheck spoofaxEsvCheck,
        SpoofaxEsvCompile spoofaxEsvCompile,

        ConfigureStatix configureStatix,
        CheckStatix checkStatix,
        CompileStatix compileStatix,

        ConfigureStratego configureStratego,
        CheckStratego checkStratego,
        CompileStratego compileStratego,

        CompileJava compileJava,
        UnarchiveFromJar unarchiveFromJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileLanguage);
        taskDefs.add(checkLanguageSpecification);
        taskDefs.add(compileLanguageSpecification);

        taskDefs.add(spoofaxCfgCheck);

        taskDefs.add(spoofaxSdf3Configure);
        taskDefs.add(spoofaxSdf3Check);
        taskDefs.add(spoofaxSdf3Compile);

        taskDefs.add(spoofaxEsvConfigure);
        taskDefs.add(spoofaxEsvCheck);
        taskDefs.add(spoofaxEsvCompile);

        taskDefs.add(configureStatix);
        taskDefs.add(checkStatix);
        taskDefs.add(compileStatix);

        taskDefs.add(configureStratego);
        taskDefs.add(checkStratego);
        taskDefs.add(compileStratego);

        taskDefs.add(compileJava);
        taskDefs.add(unarchiveFromJar);
        return taskDefs;
    }
}
