package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CliProjectTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language project, as adapter project depends on it.
        final LanguageProject languageProjectCompiler = LanguageProject.fromClassLoaderResources(resourceService, charset, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler);
        languageProjectCompiler.compile(TigerInputs.languageProject(shared));

        // Compile adapter project, as CLI project depends on it.
        final AdapterProject.Input adapterProjectInput = TigerInputs.adapterProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(adapterProjectInput, resourceService);
        final AdapterProject adapterProjectCompiler = AdapterProject.fromClassLoaderResources(resourceService, charset, parserCompiler, stylerCompiler, strategoRuntimeCompiler, constraintAnalyzerCompiler);
        adapterProjectCompiler.compile(adapterProjectInput);

        // Compile CLI project and test generated files.
        final CliProject.Input input = TigerInputs.cliProjectBuilder(shared, adapterProjectInput)
            .adapterProjectDependency(GradleDependency.project(":" + shared.adapterProject().coordinate().artifactId()))
            .build();
        final CliProject compiler = CliProject.fromClassLoaderResources(resourceService, charset);
        compiler.compile(input);
        assertFalse(input.settingsGradleKtsFile().isPresent());
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.gradle.config.java-application"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genMain(), "Main");
        });

        // Compile root project, which links together language and adapter project, and build it.
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProjectBuilder(shared)
            .addIncludedProjects(
                shared.languageProject().coordinate().artifactId(),
                shared.adapterProject().coordinate().artifactId(),
                shared.cliProject().coordinate().artifactId()
            )
            .build()
        );
        fileAssertions.asserts(rootProjectOutput.baseDirectory(), (a) -> a.assertGradleBuild("buildAll"));
    }
}
