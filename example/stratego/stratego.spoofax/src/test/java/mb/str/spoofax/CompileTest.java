package mb.str.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.resource.fs.FSResource;
import mb.str.spoofax.task.StrategoCompile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testParseTask() throws Exception {
        final FSResource projectDir = rootDirectory.appendRelativePath("project").createDirectory(true);
        final FSResource mainFile = createTextFile(projectDir, "module a rules s = id", "a.str");
        final FSResource outputDir = rootDirectory.appendRelativePath("output/mb/test").createDirectory(true);

        try(final MixedSession session = newSession()) {
            @SuppressWarnings("ConstantConditions") final Result<None, ?> result = session.require(compile.createTask(new StrategoCompile.Args(
                projectDir.getPath(),
                mainFile.getPath(),
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                outputDir.getPath(),
                "mb.test",
                new ArrayList<>()
            )));
            assertTrue(result.isOk());
            assertTrue(outputDir.exists());
            assertTrue(outputDir.isDirectory());
            final FSResource interopRegistererJavaFile = outputDir.appendRelativePath("InteropRegisterer.java");
            assertTrue(interopRegistererJavaFile.exists());
            assertTrue(interopRegistererJavaFile.isFile());
            assertTrue(interopRegistererJavaFile.readString().contains("InteropRegisterer"));
            final FSResource mainJavaFile = outputDir.appendRelativePath("Main.java");
            assertTrue(mainJavaFile.exists());
            assertTrue(mainJavaFile.isFile());
            assertTrue(mainJavaFile.readString().contains("Main"));
            final FSResource strategyJavaFile = outputDir.appendRelativePath("s_0_0.java");
            assertTrue(strategyJavaFile.exists());
            assertTrue(strategyJavaFile.isFile());
            assertTrue(strategyJavaFile.readString().contains("s_0_0"));
            final FSResource testJavaFile = outputDir.appendRelativePath("test.java");
            assertTrue(testJavaFile.exists());
            assertTrue(testJavaFile.isFile());
            assertTrue(testJavaFile.readString().contains("test"));
        }
    }
}
