package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StrategoRuntimeCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final StrategoRuntimeCompiler.LanguageProjectInput input = TigerInputs.strategoRuntimeLanguageProjectInput(shared, languageProject).build();

        strategoRuntimeCompiler.compileLanguageProject(input);
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genFactory(), "TigerStrategoRuntimeBuilderFactory");
        });
    }
}
