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
        final LanguageProject.Input languageProjectCompilerInput = TigerInputs.languageProject(shared);
        final Parser.Input parserCompilerInput = TigerInputs.parser(shared, languageProjectCompilerInput.project());
        final Styler.Input stylerCompilerInput = TigerInputs.styler(shared, languageProjectCompilerInput.project());
        final StrategoRuntime.Input strategoRuntimeBuilderCompilerInput = TigerInputs.strategoRuntime(shared, languageProjectCompilerInput.project());
        final ConstraintAnalyzer.Input constraintAnalyzerCompilerInput = TigerInputs.constraintAnalyzer(shared, languageProjectCompilerInput.project());
        final MainMultiProject.Input input = MainMultiProject.Input.builder()
            .languageProject(languageProjectCompilerInput)
            .parser(parserCompilerInput)
            .styler(stylerCompilerInput)
            .strategoRuntimeBuilder(strategoRuntimeBuilderCompilerInput)
            .constraintAnalyzer(constraintAnalyzerCompilerInput)
            .build();

        final MainMultiProject compiler = MainMultiProject.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final MainMultiProject.Output output = compiler.compile(input, charset);
        final File languageProjectDirectory = ((FSResource)resourceService.getHierarchicalResource(output.languageProject().baseDirectory())).getJavaPath().toFile();

        // noinspection CaughtExceptionImmediatelyRethrown
        try(final ProjectConnection connection = GradleConnector.newConnector()
            .forProjectDirectory(languageProjectDirectory)
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
