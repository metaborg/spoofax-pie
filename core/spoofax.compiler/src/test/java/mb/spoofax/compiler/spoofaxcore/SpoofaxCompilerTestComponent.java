package mb.spoofax.compiler.spoofaxcore;

import dagger.Component;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;

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
