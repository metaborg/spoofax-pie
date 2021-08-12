package mb.spoofax.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.GetSourceFilesAdapterCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.adapter.ReferenceResolutionAdapterCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ExportsLanguageCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.util.HashSet;
import java.util.Set;

@Module
public class SpoofaxCompilerModule {
    private final TemplateCompiler templateCompiler;

    public SpoofaxCompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }

    @Provides @SpoofaxCompilerScope
    TemplateCompiler provideTemplateCompiler() {
        return templateCompiler;
    }

    @Provides @SpoofaxCompilerQualifier @SpoofaxCompilerScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        LanguageProjectCompiler languageProjectCompiler,
        ClassLoaderResourcesCompiler classloaderResourcesCompiler,
        ParserLanguageCompiler parserLanguageCompiler,
        StylerLanguageCompiler stylerLanguageCompiler,
        ConstraintAnalyzerLanguageCompiler constraintAnalyzerLanguageCompiler,
        MultilangAnalyzerLanguageCompiler multilangAnalyzerLanguageCompiler,
        StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler,
        CompleterLanguageCompiler completerLanguageCompiler,
        ExportsLanguageCompiler exportsLanguageCompiler,

        AdapterProjectCompiler adapterProjectCompiler,
        ParserAdapterCompiler parserAdapterCompiler,
        StylerAdapterCompiler stylerAdapterCompiler,
        ConstraintAnalyzerAdapterCompiler constraintAnalyzerAdapterCompiler,
        MultilangAnalyzerAdapterCompiler multilangAnalyzerAdapterCompiler,
        CompleterAdapterCompiler completerAdapterCompiler,
        StrategoRuntimeAdapterCompiler strategoRuntimeAdapterCompiler,
        ReferenceResolutionAdapterCompiler referenceResolutionAdapterCompiler,
        GetSourceFilesAdapterCompiler getSourceFilesAdapterCompiler,

        CliProjectCompiler cliProjectCompiler,
        EclipseProjectCompiler eclipseProjectCompiler,
        IntellijProjectCompiler intellijProjectCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(classloaderResourcesCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(constraintAnalyzerLanguageCompiler);
        taskDefs.add(multilangAnalyzerLanguageCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        taskDefs.add(completerLanguageCompiler);
        taskDefs.add(exportsLanguageCompiler);

        taskDefs.add(adapterProjectCompiler);
        taskDefs.add(parserAdapterCompiler);
        taskDefs.add(stylerAdapterCompiler);
        taskDefs.add(constraintAnalyzerAdapterCompiler);
        taskDefs.add(multilangAnalyzerAdapterCompiler);
        taskDefs.add(strategoRuntimeAdapterCompiler);
        taskDefs.add(completerAdapterCompiler);
        taskDefs.add(referenceResolutionAdapterCompiler);
        taskDefs.add(getSourceFilesAdapterCompiler);

        taskDefs.add(cliProjectCompiler);
        taskDefs.add(eclipseProjectCompiler);
        taskDefs.add(intellijProjectCompiler);

        return taskDefs;
    }
}
