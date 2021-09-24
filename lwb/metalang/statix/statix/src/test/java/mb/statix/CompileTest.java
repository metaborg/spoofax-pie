package mb.statix;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.statix.task.StatixCompileModule;
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
            final Result<Option<StatixCompileModule.Output>, ?> mainResult = session.require(compileModule.createTask(new StatixCompileModule.Input(config.rootDirectory, mainFile.getPath(), config.sourceFileOrigins)));
            assertTrue(mainResult.isOk());
            final Option<StatixCompileModule.Output> mainOutputOpt = mainResult.unwrap();
            assertTrue(mainOutputOpt.isSome());
            final StatixCompileModule.Output mainOutput = mainOutputOpt.unwrap();
            assertEquals("src-gen/statix/main.spec.aterm", mainOutput.relativeOutputPath);
            assertTrue(TermUtils.isAppl(mainOutput.spec, "FileSpec", 6));

            final Result<Option<StatixCompileModule.Output>, ?> programResult = session.require(compileModule.createTask(new StatixCompileModule.Input(config.rootDirectory, programFile.getPath(), config.sourceFileOrigins)));
            assertTrue(programResult.isOk());
            final Option<StatixCompileModule.Output> programOutputOpt = programResult.unwrap();
            assertTrue(programOutputOpt.isSome());
            final StatixCompileModule.Output programOutput = programOutputOpt.unwrap();
            assertEquals("src-gen/statix/program.spec.aterm", programOutput.relativeOutputPath);
            assertTrue(TermUtils.isAppl(programOutput.spec, "FileSpec", 6));
        }
    }
}
