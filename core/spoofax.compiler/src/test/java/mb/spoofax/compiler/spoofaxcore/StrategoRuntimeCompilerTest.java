package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class StrategoRuntimeCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final StrategoRuntimeLanguageCompiler.Input input = inputs.strategoRuntimeLanguageCompilerInput();
        try(MixedSession session = pie.newSession()) {
            session.require(component.getStrategoRuntimeLanguageCompiler().createTask(input));
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(input.genStrategoRuntimeBuilderFactory(), "TigerStrategoRuntimeBuilderFactory");
            });
        }
    }
}
