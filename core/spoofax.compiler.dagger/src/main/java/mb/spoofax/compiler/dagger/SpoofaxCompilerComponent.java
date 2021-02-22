package mb.spoofax.compiler.dagger;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;

import java.util.Set;

@SpoofaxCompilerScope
@Component(
    modules = {
        SpoofaxCompilerModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class
    }
)
public interface SpoofaxCompilerComponent extends TaskDefsProvider {
    ClassLoaderResourcesCompiler getClassloaderResourcesCompiler();


    LanguageProjectCompiler getLanguageProjectCompiler();

    ParserLanguageCompiler getParserLanguageCompiler();

    StylerLanguageCompiler getStylerLanguageCompiler();

    CompleterLanguageCompiler getCompleterLanguageCompiler();

    StrategoRuntimeLanguageCompiler getStrategoRuntimeLanguageCompiler();

    ConstraintAnalyzerLanguageCompiler getConstraintAnalyzerLanguageCompiler();

    MultilangAnalyzerLanguageCompiler getMultilangAnalyzerLanguageCompiler();


    AdapterProjectCompiler getAdapterProjectCompiler();

    ParserAdapterCompiler getParserAdapterCompiler();

    StylerAdapterCompiler getStylerAdapterCompiler();

    CompleterAdapterCompiler getCompleterAdapterCompiler();

    StrategoRuntimeAdapterCompiler getStrategoRuntimeAdapterCompiler();

    ConstraintAnalyzerAdapterCompiler getConstraintAnalyzerAdapterCompiler();

    MultilangAnalyzerAdapterCompiler getMultilangAnalyzerAdapterCompiler();


    CliProjectCompiler getCliProjectCompiler();

    EclipseProjectCompiler getEclipseProjectCompiler();

    IntellijProjectCompiler getIntellijProjectCompiler();


    @Override @SpoofaxCompilerQualifier Set<TaskDef<?, ?>> getTaskDefs();
}
