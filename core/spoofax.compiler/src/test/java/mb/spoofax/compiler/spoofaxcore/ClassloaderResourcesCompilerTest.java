package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.language.ClassloaderResourcesCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class ClassloaderResourcesCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final ClassloaderResourcesCompiler.Input input = inputs.languageProjectCompilerInput().classloaderResources();
        try(MixedSession session = pie.newSession()) {
            session.require(component.getClassloaderResourcesCompiler().createTask(input));
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(input.classloaderResources(), "TigerClassloaderResources");
            });
        }
    }
}
