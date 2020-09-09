package mb.spoofax.compiler.spoofaxcore;

import dagger.Component;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.language.ClassloaderResourcesCompiler;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.adapter.MultilangAnalyzerAdapterCompiler;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;

import javax.inject.Singleton;

@Singleton @Component(modules = {SpoofaxCompilerModule.class, SpoofaxCompilerTestModule.class})
public interface SpoofaxCompilerTestComponent extends SpoofaxCompilerComponent {
    ResourceService getResourceService();

    Pie getPie();


    ClassloaderResourcesCompiler getClassloaderResourcesCompiler();


    ParserLanguageCompiler getParserLanguageCompiler();

    StylerLanguageCompiler getStylerLanguageCompiler();

    CompleterLanguageCompiler getCompleterLanguageCompiler();

    StrategoRuntimeLanguageCompiler getStrategoRuntimeLanguageCompiler();

    ConstraintAnalyzerLanguageCompiler getConstraintAnalyzerLanguageCompiler();

    MultilangAnalyzerLanguageCompiler getMultilangAnalyzerLanguageCompiler();


    ParserAdapterCompiler getParserAdapterCompiler();

    StylerAdapterCompiler getStylerAdapterCompiler();

    CompleterAdapterCompiler getCompleterAdapterCompiler();

    StrategoRuntimeAdapterCompiler getStrategoRuntimeAdapterCompiler();

    ConstraintAnalyzerAdapterCompiler getConstraintAnalyzerAdapterCompiler();

    MultilangAnalyzerAdapterCompiler getMultilangAnalyzerAdapterCompiler();
}
