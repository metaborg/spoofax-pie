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

import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageProjectTest {
    @Test void testCompilerStandalone(@TempDir Path temporaryDirectoryPath) throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);

        final LanguageProject.Input input = TigerInputs.languageProjectBuilder(TigerInputs.shared(baseDirectory))
            .standaloneProject(true)
            .build();

        final Charset charset = StandardCharsets.UTF_8;
        final Parser parserCompiler = Parser.fromClassLoaderResources(resourceService, charset);
        final Styler stylerCompiler = Styler.fromClassLoaderResources(resourceService, charset);
        final StrategoRuntime strategoRuntimeCompiler = StrategoRuntime.fromClassLoaderResources(resourceService, charset);
        final ConstraintAnalyzer constraintAnalyzerCompiler = ConstraintAnalyzer.fromClassLoaderResources(resourceService, charset);
        final LanguageProject compiler = LanguageProject.fromClassLoaderResources(resourceService, charset, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler);
        final LanguageProject.Output output = compiler.compile(input);

        assertTrue(output.settingsGradleKtsFile().isPresent());
        final FileAssertions settingsGradleKtsFile = new FileAssertions(resourceService.getHierarchicalResource(output.settingsGradleKtsFile().get()));
        settingsGradleKtsFile.assertExists();
        settingsGradleKtsFile.assertName("settings.gradle.kts");
        settingsGradleKtsFile.assertContains("gradlePluginPortal()");

        final FileAssertions buildGradleKtsFile = new FileAssertions(resourceService.getHierarchicalResource(output.buildGradleKtsFile()));
        buildGradleKtsFile.assertExists();
        buildGradleKtsFile.assertContains("mb/tiger");

        final FileAssertions projectDirectory = new FileAssertions(baseDirectory, resourceService);
        projectDirectory.assertExists();
        projectDirectory.assertGradleBuild("build");
    }
}
