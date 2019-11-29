package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
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

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(output.buildGradleKtsFile());
        assertTrue(buildGradleKtsFile.exists());
        assertTrue(buildGradleKtsFile.readString(charset).contains("mb/tiger"));

        final File baseDirectoryFile = ((FSResource)resourceService.getHierarchicalResource(output.baseDirectory())).getJavaPath().toFile();
        // noinspection CaughtExceptionImmediatelyRethrown
        try(final ProjectConnection connection = GradleConnector.newConnector()
            .forProjectDirectory(baseDirectoryFile)
            .connect()
        ) {
            connection.newBuild()
                .forTasks("build")
                .addArguments("--quiet") // Only print important information messages and errors.
                .setStandardOutput(System.out).setStandardError(System.err) // Redirect standard out and err.
                .run();
        } catch(Throwable e) {
            throw e; // Place breakpoint here to debug failures.
        }
    }
}
