package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class StylerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final StylerLanguageCompiler.Input languageProjectInput = inputs.stylerLanguageCompilerInput();
            session.require(component.getStylerLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(languageProjectInput.genRules(), "TigerStylingRules");
                s.assertPublicJavaClass(languageProjectInput.genStyler(), "TigerStyler");
                s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerStylerFactory");
            });

            final StylerAdapterCompiler.Input adapterProjectInput = inputs.stylerAdapterCompilerInput();
            session.require(component.getStylerAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.genStyleTaskDef(), "TigerStyle");
            });
        }
    }
}
