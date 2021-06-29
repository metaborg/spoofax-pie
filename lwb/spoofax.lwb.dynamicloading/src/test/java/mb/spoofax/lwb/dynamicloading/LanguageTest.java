package mb.spoofax.lwb.dynamicloading;

import mb.common.message.KeyedMessages;
import mb.common.util.ExceptionPrinter;
import mb.pie.api.MixedSession;
import mb.resource.hierarchical.ResourcePath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

public class LanguageTest extends TestBase {
    @BeforeEach void setup(@TempDir Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
    }

    @AfterEach void teardown() throws Exception {
        super.teardown();
    }

    @Test void testCompileLayoutSensitiveLanguage() throws Exception {
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/dynamicloading/layout_sensitive");
        try(final MixedSession session = newSession()) {
            final ResourcePath rootDirectory = temporaryDirectory.getPath();
            try(final DynamicLanguage ignored = requireDynamicLoad(session, rootDirectory)) {
                final KeyedMessages messages = requireSptCheck(session, rootDirectory);
                assertNoErrors(messages, "SPT tests to succeed, but one or more failed");
            }
        } catch(Exception e) {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(temporaryDirectory);
            System.err.println(exceptionPrinter.printExceptionToString(e));
            throw e;
        }
    }
}
