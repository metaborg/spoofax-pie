package mb.str.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.str.spoofax.task.StrategoCompile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testCompileTask() throws Exception {
        final FSResource projectDir = rootDirectory.appendRelativePath("project").createDirectory(true);
        final FSResource mainFile = createTextFile(projectDir, "module hello imports libstratego-lib world rules hello = !$[hello [<world>]]; debug", "hello.str");
        final FSResource worldFile = createTextFile(projectDir, "module world imports libstratego-lib rules world = !\"world\"", "world.str");
        final FSResource outputDir = rootDirectory.appendRelativePath("output/mb/test").createDirectory(true);

        try(final MixedSession session = newSession()) {
            final ArrayList<ResourcePath> includeDirs = new ArrayList<>();
            includeDirs.add(projectDir.getPath());
            final ArrayList<String> builtinLibs = new ArrayList<>();
            builtinLibs.add("stratego-lib");
            @SuppressWarnings("ConstantConditions") final Result<None, ?> result = session.require(compile.createTask(new StrategoCompile.Args(
                projectDir.getPath(),
                mainFile.getPath(),
                includeDirs,
                builtinLibs,
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
            final FSResource mainStrategyJavaFile = outputDir.appendRelativePath("hello_0_0.java");
            assertTrue(mainStrategyJavaFile.exists());
            assertTrue(mainStrategyJavaFile.isFile());
            assertTrue(mainStrategyJavaFile.readString().contains("hello_0_0"));
            final FSResource worldStrategyJavaFile = outputDir.appendRelativePath("world_0_0.java");
            assertTrue(worldStrategyJavaFile.exists());
            assertTrue(worldStrategyJavaFile.isFile());
            assertTrue(worldStrategyJavaFile.readString().contains("world_0_0"));
            final FSResource testPackageJavaFile = outputDir.appendRelativePath("test.java");
            assertTrue(testPackageJavaFile.exists());
            assertTrue(testPackageJavaFile.isFile());
            assertTrue(testPackageJavaFile.readString().contains("test"));
        }
    }
}
