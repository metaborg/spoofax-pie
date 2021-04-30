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
import mb.spoofax.lwb.compiler.esv.CheckEsv;
import mb.spoofax.lwb.compiler.esv.CompileEsv;
import mb.spoofax.lwb.compiler.esv.ConfigureEsv;
import mb.spoofax.lwb.compiler.sdf3.CheckSdf3;
import mb.spoofax.lwb.compiler.sdf3.CompileSdf3;
import mb.spoofax.lwb.compiler.sdf3.ConfigureSdf3;
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

        ConfigureSdf3 configureSdf3,
        CheckSdf3 checkSdf3,
        CompileSdf3 compileSdf3,

        ConfigureEsv configureEsv,
        CheckEsv checkEsv,
        CompileEsv compileEsv,

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

        taskDefs.add(configureSdf3);
        taskDefs.add(checkSdf3);
        taskDefs.add(compileSdf3);

        taskDefs.add(configureEsv);
        taskDefs.add(checkEsv);
        taskDefs.add(compileEsv);

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
