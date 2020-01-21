package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageProjectTest extends TestBase {
    @Test void testCompilerDefaultsStandalone(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final LanguageProject.Input input = TigerInputs.languageProjectBuilder(TigerInputs.shared(baseDirectory))
            .standaloneProject(true)
            .build();

        languageProjectCompiler.compile(input);
        assertTrue(input.settingsGradleKtsFile().isPresent());
        fileAssertions.asserts(input.settingsGradleKtsFile().get(), (a) -> a.assertContains("gradlePluginPortal()"));
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("mb/tiger"));
        fileAssertions.asserts(baseDirectory, (a) -> a.assertGradleBuild("build"));
    }
}
