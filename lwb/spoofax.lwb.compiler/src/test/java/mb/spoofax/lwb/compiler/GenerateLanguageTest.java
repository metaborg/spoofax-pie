package mb.spoofax.lwb.compiler;

import mb.spoofax.lwb.compiler.generator.LanguageProjectGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GenerateLanguageTest extends TestBase {
    @BeforeEach void setup(@TempDir Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
    }

    @AfterEach void teardown() throws Exception {
        super.teardown();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testGenerateAndCompileLanguage(boolean multiFileAnalysis) throws Exception {
        final LanguageProjectGenerator.Input input = LanguageProjectGenerator.Input.builder()
            .rootDirectory(rootDirectory.getPath())
            .id("chars")
            .name("Chars")
            .javaClassIdPrefix("Chars")
            .addFileExtensions("chars")
            .multiFileAnalysis(multiFileAnalysis)
            .build();
        compiler.compiler.component.getLanguageProjectGenerator().generate(input);
        assertTrue(rootDirectory.exists());

        // Check and compile generated language.
        checkAndCompileLanguage();

        // Recreate compiler (including PIE component) and compile again to test serialize/deserialize roundtrip.
        recreateCompiler();
        checkAndCompileLanguage();
    }
}
