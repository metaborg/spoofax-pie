package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class StrategoRuntimeCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final StrategoRuntimeLanguageCompiler.Input input = inputs.strategoRuntimeLanguageCompilerInput();
        component.getStrategoRuntimeLanguageCompiler().compile(new MockExecContext(), input);
        fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(input.baseStrategoRuntimeBuilderFactory(), "TigerStrategoRuntimeBuilderFactory");
        });
    }
}
