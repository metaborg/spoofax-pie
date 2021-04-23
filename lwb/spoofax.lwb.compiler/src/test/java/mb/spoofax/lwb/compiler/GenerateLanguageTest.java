package mb.spoofax.lwb.compiler;

import mb.common.util.ExceptionPrinter;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.spoofax.lwb.compiler.generator.LanguageProjectGenerator;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GenerateLanguageTest extends TestBase {
    final LanguageProjectGenerator languageProjectGenerator = compiler.compiler.component.getLanguageProjectGenerator();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testGenerateAndCompileLanguage(boolean multiFileAnalysis, @TempDir Path temporaryDirectoryPath) throws Exception {
        final FSResource rootDirectory = new FSResource(temporaryDirectoryPath).appendRelativePath("chars");
        final LanguageProjectGenerator.Input input = LanguageProjectGenerator.Input.builder()
            .rootDirectory(rootDirectory.getPath())
            .id("chars")
            .name("Chars")
            .javaClassIdPrefix("Chars")
            .addFileExtensions("chars")
            .multiFileAnalysis(multiFileAnalysis)
            .build();
        languageProjectGenerator.generate(input);
        assertTrue(rootDirectory.exists());
        try(final MixedSession session = pie.newSession()) {
            final CompileLanguage.Args args = CompileLanguage.Args.builder()
                .rootDirectory(rootDirectory.getPath())
                .build();
            session.require(compileLanguage.createTask(args)).unwrap();
        } catch(CompileLanguageException e) {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(rootDirectory);
            System.err.println(exceptionPrinter.printExceptionToString(e));
            throw e;
        }
    }
}
