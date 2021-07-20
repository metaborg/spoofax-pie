package mb.spoofax.lwb.compiler;

import mb.pie.api.MixedSession;
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
        try(final MixedSession session = pieComponent.getPie().newSession()) {
            final CompileLanguage.Args args = CompileLanguage.Args.builder()
                .rootDirectory(rootDirectory.getPath())
                .build();
            session.require(compileLanguage.createTask(args)).unwrap();
        } catch(CompileLanguageException e) {
            System.err.println(exceptionPrinter.printExceptionToString(e));
            throw e;
        }
    }
}
