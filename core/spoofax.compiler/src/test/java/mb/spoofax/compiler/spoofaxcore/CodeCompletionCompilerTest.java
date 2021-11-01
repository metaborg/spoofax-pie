package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.adapter.CodeCompletionAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CodeCompletionCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final CodeCompletionAdapterCompiler.Input adapterProjectInput = inputs.codeCompletionAdapterCompilerInput();
        component.getCodeCompletionAdapterCompiler().compile(new MockExecContext(), adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.baseCodeCompletionTaskDef(), "TigerCodeCompletionTaskDef");
        });
    }
}
