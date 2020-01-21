package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StrategoRuntimeTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory);
        final StrategoRuntime.Input input = TigerInputs.strategoRuntime(shared);

        strategoRuntimeCompiler.compileLanguageProject(input);
        fileAssertions.scopedExists(input.languageClassesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genFactory(), "TigerStrategoRuntimeBuilderFactory");
        });
    }
}
