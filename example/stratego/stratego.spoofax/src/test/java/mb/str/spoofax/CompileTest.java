package mb.str.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.resource.fs.FSResource;
import mb.str.spoofax.task.StrategoCompile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Disabled("Not working yet") @Test void testParseTask() throws Exception {
        final FSResource projectDir = rootDirectory.appendRelativePath("project").createDirectory(true);
        final FSResource mainFile = createTextFile(projectDir, "module a", "a.str");
        final FSResource outputDir = rootDirectory.appendRelativePath("output").createDirectory(true);

        try(final MixedSession session = newSession()) {
            @SuppressWarnings("ConstantConditions") final Result<None, ?> result = session.require(compile.createTask(new StrategoCompile.Args(
                projectDir.getPath(),
                mainFile.getPath(),
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                outputDir.getPath(),
                null,
                new ArrayList<>()
            )));
            assertTrue(result.isOk());
        }
    }
}
