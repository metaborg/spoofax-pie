package mb.spoofax.compiler.spoofaxcore;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

class RootProjectTest {
    @Test void testCompilerDefault(@TempDir Path temporaryDirectoryPath) throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Charset charset = StandardCharsets.UTF_8;

        final RootProject.Input input = TigerInputs.rootProject(TigerInputs.shared(baseDirectory));

        final RootProject compiler = RootProject.fromClassLoaderResources(resourceService, charset);
        final RootProject.Output output = compiler.compile(input);

        final FileAssertions settingsGradleKtsFile = new FileAssertions(resourceService.getHierarchicalResource(input.settingsGradleKtsFile()));
        settingsGradleKtsFile.assertExists();
        settingsGradleKtsFile.assertName("settings.gradle.kts");
        settingsGradleKtsFile.assertContains(input.shared().rootProject().coordinate().artifactId());

        final FileAssertions buildGradleKtsFile = new FileAssertions(resourceService.getHierarchicalResource(input.buildGradleKtsFile()));
        buildGradleKtsFile.assertExists();
        buildGradleKtsFile.assertContains("org.metaborg.gradle.config.root-project");

        final FileAssertions projectDirectory = new FileAssertions(baseDirectory, resourceService);
        projectDirectory.assertExists();
        projectDirectory.assertGradleBuild();
    }
}
