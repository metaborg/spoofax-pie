package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.spoofax2.language.Spoofax2ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2ParserLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2StylerLanguageCompiler;

import java.util.HashSet;
import java.util.Set;

@Module
public class Spoofax2CompilerModule {
    @Provides @Spoofax2CompilerScope @Spoofax2CompilerQualifier @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        Spoofax2LanguageProjectCompiler languageProjectCompiler,
        Spoofax2ParserLanguageCompiler parserLanguageCompiler,
        Spoofax2StylerLanguageCompiler stylerLanguageCompiler,
        Spoofax2ConstraintAnalyzerLanguageCompiler constraintAnalyzerLanguageCompiler,
        Spoofax2MultilangAnalyzerLanguageCompiler multilangAnalyzerLanguageCompiler,
        Spoofax2StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(constraintAnalyzerLanguageCompiler);
        taskDefs.add(multilangAnalyzerLanguageCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        return taskDefs;
    }
}
