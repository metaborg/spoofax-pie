package mb.spoofax.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.ClassloaderResourcesCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.platform.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
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
        taskDefs.add(strategoRuntimeAdapterCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        taskDefs.add(stylerAdapterCompiler);
        taskDefs.add(stylerLanguageCompiler);
        return taskDefs;
    }
}
