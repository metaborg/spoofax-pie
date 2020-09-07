package mb.spoofax.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.ClassloaderResourcesCompiler;
import mb.spoofax.compiler.spoofaxcore.CliProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.CompleterAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.CompleterLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.EclipseProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.IntellijProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.ParserAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.ParserLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.RootProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.StylerAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.StylerLanguageCompiler;
import mb.spoofax.compiler.util.TemplateCompiler;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module
public class SpoofaxCompilerModule {
    private final TemplateCompiler templateCompiler;

    public SpoofaxCompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }

    @Provides @Singleton public TemplateCompiler provideTemplateCompiler() { return templateCompiler; }

    @Provides @Singleton @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefsSet(
        AdapterProjectCompiler adapterProjectCompiler,
        ClassloaderResourcesCompiler classloaderResourcesCompiler,
        CliProjectCompiler cliProjectCompiler,
        CompleterAdapterCompiler completerAdapterCompiler,
        CompleterLanguageCompiler completerLanguageCompiler,
        ConstraintAnalyzerAdapterCompiler constraintAnalyzerAdapterCompiler,
        ConstraintAnalyzerLanguageCompiler constraintAnalyzerLanguageCompiler,
        EclipseExternaldepsProjectCompiler eclipseExternaldepsProjectCompiler,
        EclipseProjectCompiler eclipseProjectCompiler,
        IntellijProjectCompiler intellijProjectCompiler,
        LanguageProjectCompiler languageProjectCompiler,
        MultilangAnalyzerAdapterCompiler multilangAnalyzerAdapterCompiler,
        MultilangAnalyzerLanguageCompiler multilangAnalyzerLanguageCompiler,
        ParserAdapterCompiler parserAdapterCompiler,
        ParserLanguageCompiler parserLanguageCompiler,
        RootProjectCompiler rootProjectCompiler,
        StrategoRuntimeAdapterCompiler strategoRuntimeAdapterCompiler,
        StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler,
        StylerAdapterCompiler stylerAdapterCompiler,
        StylerLanguageCompiler stylerLanguageCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(adapterProjectCompiler);
        taskDefs.add(classloaderResourcesCompiler);
        taskDefs.add(cliProjectCompiler);
        taskDefs.add(completerAdapterCompiler);
        taskDefs.add(completerLanguageCompiler);
        taskDefs.add(constraintAnalyzerAdapterCompiler);
        taskDefs.add(constraintAnalyzerLanguageCompiler);
        taskDefs.add(eclipseExternaldepsProjectCompiler);
        taskDefs.add(eclipseProjectCompiler);
        taskDefs.add(intellijProjectCompiler);
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(multilangAnalyzerAdapterCompiler);
        taskDefs.add(multilangAnalyzerLanguageCompiler);
        taskDefs.add(parserAdapterCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(rootProjectCompiler);
        taskDefs.add(strategoRuntimeAdapterCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        taskDefs.add(stylerAdapterCompiler);
        taskDefs.add(stylerLanguageCompiler);
        return taskDefs;
    }
}
