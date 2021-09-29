package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.CodeCompletionAdapterCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CodeCompletionCompilerTest extends TestBase {
    @Disabled("Unclear why this fails")
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final CodeCompletionAdapterCompiler.Input adapterProjectInput = inputs.codeCompletionAdapterCompilerInput();
            session.require(component.getCompleterAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.baseCodeCompletionTaskDef(), "TigerCompleteTaskDef");
            });
        }
    }
}
