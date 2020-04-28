package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class RootProjectCompilerTest extends TestBase {
    @Test void testCompilerDefault(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final RootProjectCompiler.Input input = TigerInputs.rootProjectInput(shared).build();

        rootProjectCompiler.compile(input);
        fileAssertions.asserts(input.settingsGradleKtsFile(), (a) -> a.assertContains(input.project().coordinate().artifactId()));
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.gradle.config.root-project"));
//        fileAssertions.asserts(baseDirectory, (a) -> a.assertGradleBuild("buildAll"));
    }
}
