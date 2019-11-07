package mb.spoofax.compiler.spoofaxcore;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.fs.FSResourceRegistry;
import mb.spoofax.compiler.spoofaxcore.util.CommonInputs;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

class AllCompilerTest {
    @Test void testCompilerDefault(@TempDir Path temporaryDirectoryPath) throws IOException, Throwable {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final LanguageProjectCompiler.Input languageProjectCompilerInput = CommonInputs.tigerLanguageProjectCompilerInput(shared);
        final ParserCompiler.Input parserCompilerInput = CommonInputs.tigerParserCompilerInput(shared, languageProjectCompilerInput.project());
        final StylerCompiler.Input stylerCompilerInput = CommonInputs.tigerStylerCompilerInput(shared, languageProjectCompilerInput.project());
        final AllCompiler.Input input = AllCompiler.Input.builder()
            .languageProject(languageProjectCompilerInput)
            .parser(parserCompilerInput)
            .styler(stylerCompilerInput)
            .build();

        final AllCompiler compiler = AllCompiler.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final AllCompiler.Output output = compiler.compile(input, charset);
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
