package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3ParserLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3StrategoRuntimeLanguageCompiler;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module public class Spoofax3CompilerModule {
    @Provides @Spoofax3CompilerScope @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefsSet(
        Spoofax3LanguageProjectCompiler languageProjectCompiler,
        Spoofax3ParserLanguageCompiler parserLanguageCompiler,
        Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        return taskDefs;
    }
}
