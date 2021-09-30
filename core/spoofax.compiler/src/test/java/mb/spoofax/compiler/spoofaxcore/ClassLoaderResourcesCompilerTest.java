package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.language.ClassLoaderResourcesCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class ClassLoaderResourcesCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final ClassLoaderResourcesCompiler.Input input = inputs.languageProjectCompilerInput().classLoaderResources();
        component.getClassloaderResourcesCompiler().compile(new MockExecContext(), input);
        fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(input.classLoaderResources(), "TigerClassLoaderResources");
        });
    }
}
