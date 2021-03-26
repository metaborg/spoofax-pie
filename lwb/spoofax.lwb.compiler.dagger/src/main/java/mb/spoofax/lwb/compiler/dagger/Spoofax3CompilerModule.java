package mb.spoofax.lwb.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.pie.task.java.CompileJava;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageToJavaClassPath;
import mb.spoofax.lwb.compiler.CompileLanguageWithCfgToJavaClassPath;
import mb.spoofax.lwb.compiler.esv.CompileEsv;
import mb.spoofax.lwb.compiler.esv.ConfigureEsv;
import mb.spoofax.lwb.compiler.sdf3.CompileSdf3;
import mb.spoofax.lwb.compiler.sdf3.ConfigureSdf3;
import mb.spoofax.lwb.compiler.statix.CompileStatix;
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

    @Provides @Spoofax3CompilerScope
    static CompileJava provideCompileJava() {
        return new CompileJava();
    }


    @Provides @Spoofax3CompilerQualifier @Spoofax3CompilerScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        CompileLanguageToJavaClassPath compileLanguageToJavaClassPath,
        CompileLanguageWithCfgToJavaClassPath compileLanguageWithCfgToJavaClassPath,
        CompileLanguage languageProjectCompiler,

        ConfigureSdf3 configureSdf3,
        CompileSdf3 compileSdf3,
        ConfigureEsv configureEsv,
        CompileEsv compileEsv,
        CompileStatix compileStatix,
        ConfigureStratego configureStratego,
        CompileStratego compileStratego,

        CompileJava compileJava,
        UnarchiveFromJar unarchiveFromJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileLanguageToJavaClassPath);
        taskDefs.add(compileLanguageWithCfgToJavaClassPath);
        taskDefs.add(languageProjectCompiler);

        taskDefs.add(configureSdf3);
        taskDefs.add(compileSdf3);
        taskDefs.add(configureEsv);
        taskDefs.add(compileEsv);
        taskDefs.add(compileStatix);
        taskDefs.add(configureStratego);
        taskDefs.add(compileStratego);

        taskDefs.add(compileJava);
        taskDefs.add(unarchiveFromJar);
        return taskDefs;
    }
}
