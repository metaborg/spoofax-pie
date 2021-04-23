package mb.spoofax.lwb.compiler;

import mb.common.util.ExceptionPrinter;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class CompileLanguageTest extends TestBase {
    @Test void testCompileCharsLanguage(@TempDir Path temporaryDirectoryPath) throws Exception {
        // Copy language specification sources to the temporary directory.
        final FSResource temporaryDirectory = new FSResource(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/compiler/chars", temporaryDirectory);
        try(final MixedSession session = pie.newSession()) {
            final CompileLanguage.Args args = CompileLanguage.Args.builder()
                .rootDirectory(temporaryDirectory.getPath())
                .build();
            session.require(compileLanguage.createTask(args)).unwrap();
        } catch(CompileLanguageException e) {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(temporaryDirectory);
            System.err.println(exceptionPrinter.printExceptionToString(e));
            throw e;
        }
    }
}
