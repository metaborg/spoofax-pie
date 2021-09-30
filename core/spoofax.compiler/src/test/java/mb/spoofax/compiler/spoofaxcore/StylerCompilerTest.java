package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class StylerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final StylerLanguageCompiler.Input languageProjectInput = inputs.stylerLanguageCompilerInput();
        component.getStylerLanguageCompiler().compile(new MockExecContext(), languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(languageProjectInput.baseStylingRules(), "TigerStylingRules");
            s.assertPublicJavaClass(languageProjectInput.baseStyler(), "TigerStyler");
            s.assertPublicJavaClass(languageProjectInput.baseStylerFactory(), "TigerStylerFactory");
        });

        final StylerAdapterCompiler.Input adapterProjectInput = inputs.stylerAdapterCompilerInput();
        component.getStylerAdapterCompiler().compile(new MockExecContext(), adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.baseStyleTaskDef(), "TigerStyle");
        });
    }
}
