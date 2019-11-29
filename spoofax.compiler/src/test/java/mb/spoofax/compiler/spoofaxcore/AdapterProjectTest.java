package mb.spoofax.compiler.spoofaxcore;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AdapterProjectTest {
    @Test void testCompiler(@TempDir Path temporaryDirectoryPath) throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Charset charset = StandardCharsets.UTF_8;

        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language project, as adapter project depends on it.
        final Parser parserCompiler = Parser.fromClassLoaderResources(resourceService, charset);
        final Styler stylerCompiler = Styler.fromClassLoaderResources(resourceService, charset);
        final StrategoRuntime strategoRuntimeCompiler = StrategoRuntime.fromClassLoaderResources(resourceService, charset);
        final ConstraintAnalyzer constraintAnalyzerCompiler = ConstraintAnalyzer.fromClassLoaderResources(resourceService, charset);
        final LanguageProject languageProjectCompiler = LanguageProject.fromClassLoaderResources(resourceService, charset, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler);
        languageProjectCompiler.compile(TigerInputs.languageProject(shared));

        // Compile adapter project.
        final AdapterProject.Input input = TigerInputs.adapterProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .build();
        final AdapterProject compiler = AdapterProject.fromClassLoaderResources(resourceService, charset);
        final AdapterProject.Output output = compiler.compile(input);

        assertFalse(output.settingsGradleKtsFile().isPresent());

        final FileAssertions buildGradleKtsFile = new FileAssertions(resourceService.getHierarchicalResource(output.buildGradleKtsFile()));
        buildGradleKtsFile.assertExists();
        buildGradleKtsFile.assertContains("org.metaborg.gradle.config.java-library");

        // Compile root project, which links together language and adapter project.
        final RootProject rootProjectCompiler = RootProject.fromClassLoaderResources(resourceService, charset);
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProject(shared));

        // Run Gradle build assertion on the root project.
        final FileAssertions rootProjectDirectory = new FileAssertions(rootProjectOutput.baseDirectory(), resourceService);
        rootProjectDirectory.assertExists();
        rootProjectDirectory.assertGradleBuild("buildAll");
    }
}
