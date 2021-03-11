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
import mb.spoofax.lwb.compiler.metalang.CompileEsv;
import mb.spoofax.lwb.compiler.metalang.CompileSdf3;
import mb.spoofax.lwb.compiler.metalang.CompileStatix;
import mb.spoofax.lwb.compiler.metalang.CompileStratego;

import java.util.HashSet;
import java.util.Set;

@Module public class Spoofax3CompilerModule {
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

        CompileSdf3 parserLanguageCompiler,
        CompileEsv stylerLanguageCompiler,
        CompileStatix constraintAnalyzerCompiler,
        CompileStratego strategoRuntimeLanguageCompiler,

        CompileJava compileJava,
        UnarchiveFromJar unarchiveFromJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileLanguageToJavaClassPath);
        taskDefs.add(compileLanguageWithCfgToJavaClassPath);
        taskDefs.add(languageProjectCompiler);

        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(constraintAnalyzerCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);

        taskDefs.add(compileJava);
        taskDefs.add(unarchiveFromJar);
        return taskDefs;
    }
}
