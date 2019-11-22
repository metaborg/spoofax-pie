package mb.spoofax.compiler.spoofaxcore;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

class MainMultiProjectTest {
    @Test void testCompilerDefault(@TempDir Path temporaryDirectoryPath) throws Throwable {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);

        final Shared shared = TigerInputs.shared(baseDirectory);
        final LanguageProject.Input languageProjectInput = TigerInputs.languageProject(shared);
        final RootProject.Input rootProjectInput = TigerInputs.rootProject(shared, languageProjectInput.project().coordinate().artifactId());
        final Parser.Input parserInput = TigerInputs.parser(shared, languageProjectInput.project());
        final Styler.Input stylerInput = TigerInputs.styler(shared, languageProjectInput.project());
        final StrategoRuntime.Input strategoRuntimeInput = TigerInputs.strategoRuntime(shared, languageProjectInput.project());
        final ConstraintAnalyzer.Input constraintAnalyzerInput = TigerInputs.constraintAnalyzer(shared, languageProjectInput.project());
        final MainMultiProject.Input input = MainMultiProject.Input.builder()
            .languageProject(languageProjectInput)
            .rootProject(rootProjectInput)
            .parser(parserInput)
            .styler(stylerInput)
            .strategoRuntimeBuilder(strategoRuntimeInput)
            .constraintAnalyzer(constraintAnalyzerInput)
            .build();

        final MainMultiProject compiler = MainMultiProject.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final MainMultiProject.Output output = compiler.compile(input, charset);
        final File baseDirectoryFile = ((FSResource)resourceService.getHierarchicalResource(output.rootProject().baseDirectory())).getJavaPath().toFile();

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
