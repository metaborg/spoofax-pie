package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.spoofax.compiler.spoofax3.language.Spoofax3ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3ParserLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3StylerLanguageCompiler;
import mb.spoofax.compiler.util.TemplateCompiler;

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

    @Provides @Spoofax3CompilerQualifier @Spoofax3CompilerScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        Spoofax3LanguageProjectCompiler languageProjectCompiler,
        Spoofax3ParserLanguageCompiler parserLanguageCompiler,
        Spoofax3StylerLanguageCompiler stylerLanguageCompiler,
        Spoofax3ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler,
        UnarchiveFromJar unarchiveFromJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(constraintAnalyzerCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        taskDefs.add(unarchiveFromJar);
        return taskDefs;
    }
}
