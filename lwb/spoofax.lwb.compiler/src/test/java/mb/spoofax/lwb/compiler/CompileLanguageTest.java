package mb.spoofax.lwb.compiler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class CompileLanguageTest extends TestBase {
    @BeforeEach void setup(@TempDir Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
    }

    @AfterEach void teardown() throws Exception {
        super.teardown();
    }

    @Test void testCompileCharsLanguage() throws Exception {
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/compiler/chars");
        checkAndCompileLanguage();
        // Recreate compiler (including PIE component) and compile again to test serialize/deserialize roundtrip.
        recreateCompiler();
        checkAndCompileLanguage();
    }
}
