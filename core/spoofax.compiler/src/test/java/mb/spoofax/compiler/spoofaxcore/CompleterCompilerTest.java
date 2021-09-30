package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CompleterCompilerTest extends TestBase {
    @Disabled("Unclear why this fails")
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final CompleterLanguageCompiler.Input languageProjectInput = inputs.completerLanguageCompilerInput();
        component.getCompleterLanguageCompiler().compile(new MockExecContext(), languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
        });

        final CompleterAdapterCompiler.Input adapterProjectInput = inputs.completerAdapterCompilerInput();
        component.getCompleterAdapterCompiler().compile(new MockExecContext(), adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.baseCompleteTaskDef(), "TigerCompleteTaskDef");
        });
    }
}
