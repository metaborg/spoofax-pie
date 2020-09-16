package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CompleterCompilerTest extends TestBase {
    @Disabled("Unclear why this fails")
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final CompleterLanguageCompiler.Input languageProjectInput = inputs.completerLanguageCompilerInput();
            session.require(component.getCompleterLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
            });

            final CompleterAdapterCompiler.Input adapterProjectInput = inputs.completerAdapterCompilerInput();
            session.require(component.getCompleterAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.genCompleteTaskDef(), "TigerCompleteTaskDef");
            });
        }
    }
}
