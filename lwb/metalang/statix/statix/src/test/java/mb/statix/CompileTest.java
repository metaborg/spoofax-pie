package mb.statix;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.statix.task.StatixCompile;
import mb.statix.task.StatixConfig;
import org.junit.jupiter.api.Test;
import org.spoofax.terms.util.TermUtils;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testCompile() throws Exception {
        final FSResource src = directory(rootDirectory, "src");
        final FSResource mainFile = textFile(src, "main.stx", "" +
            "module main\n" +
            "imports program\n" +
            "rules\n" +
            "  projectOk : scope\n" +
            "  projectOk(s).\n" +
            "\n" +
            "  fileOk : scope * Program\n" +
            "  fileOk(s, p) :-\n" +
            "    programOk(s, p).\n"
        );
        final FSResource programFile = textFile(src, "program.stx", "" +
            "module program\n" +
            "signature\n" +
            "  sorts Program constructors\n" +
            "    Program : Program\n" +
            "rules\n" +
            "  programOk : scope * Program\n" +
            "  programOk(s, Program()).\n"
        );
        final StatixConfig config = StatixConfig.createDefault(rootDirectory.getPath());

        try(final MixedSession session = newSession()) {
            final Result<StatixCompile.Output, ?> mainResult = session.require(compile.createTask(new StatixCompile.Input(mainFile.getPath(), config)));
            assertTrue(mainResult.isOk());
            final StatixCompile.Output mainOutput = mainResult.unwrap();
            assertEquals("src-gen/statix/main.spec.aterm", mainOutput.relativeOutputPath);
            assertTrue(TermUtils.isAppl(mainOutput.spec, "FileSpec", 6));

            final Result<StatixCompile.Output, ?> programResult = session.require(compile.createTask(new StatixCompile.Input(programFile.getPath(), config)));
            assertTrue(programResult.isOk());
            final StatixCompile.Output programOutput = programResult.unwrap();
            assertEquals("src-gen/statix/program.spec.aterm", programOutput.relativeOutputPath);
            assertTrue(TermUtils.isAppl(programOutput.spec, "FileSpec", 6));
        }
    }
}
