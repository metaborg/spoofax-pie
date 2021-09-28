package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.CodeCompletionAdapterCompiler;
import mb.spoofax.compiler.language.CodeCompletionLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CompleterCompilerTest extends TestBase {
    @Disabled("Unclear why this fails")
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final CodeCompletionLanguageCompiler.Input languageProjectInput = inputs.completerLanguageCompilerInput();
            session.require(component.getCompleterLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            });

            final CodeCompletionAdapterCompiler.Input adapterProjectInput = inputs.completerAdapterCompilerInput();
            session.require(component.getCompleterAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.baseCodeCompletionTaskDef(), "TigerCompleteTaskDef");
            });
        }
    }
}
