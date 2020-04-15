package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ClassloaderResourcesCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();

        final ClassloaderResourcesCompiler.LanguageProjectInput languageProjectInput = TigerInputs.classloaderResourcesLanguageProjectInput(shared, languageProject).build();
        classloaderResourcesCompiler.compileLanguageProject(languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(languageProjectInput.classloaderResources(), "TigerClassloaderResources");
        });
    }
}
