package mb.statix.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.statix.spoofax.task.StatixCompile;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testCompile() throws Exception {
        final FSResource root = createDir(rootDirectory, "stx");
        final FSResource mainFile = createTextFile(root, "" +
                "module main\n" +
                "imports program\n" +
                "rules\n" +
                "  projectOk : scope\n" +
                "  projectOk(s).\n" +
                "\n" +
                "  fileOk : scope * Program\n" +
                "  fileOk(s, p) :-\n" +
                "    programOk(s, p).\n",
            "main.stx");
        final FSResource programFile = createTextFile(root, "" +
                "module program\n" +
                "signature\n" +
                "  sorts Program constructors\n" +
                "    Program : Program\n" +
                "rules\n" +
                "  programOk : scope * Program\n" +
                "  programOk(s, Program()).\n",
            "program.stx");

        try(final MixedSession session = newSession()) {
            final Result<IStrategoTerm, ?> mainResult = session.require(compile.createTask(new StatixCompile.Input(root.getPath(), mainFile.getPath())));
            assertTrue(mainResult.isOk());
            final IStrategoTerm mainAst = mainResult.unwrap();
            assertTrue(TermUtils.isApplAt(mainAst, 1, "FileSpec", 6));

            final Result<IStrategoTerm, ?> programResult = session.require(compile.createTask(new StatixCompile.Input(root.getPath(), programFile.getPath())));
            assertTrue(programResult.isOk());
            final IStrategoTerm programAst = programResult.unwrap();
            assertTrue(TermUtils.isApplAt(programAst, 1, "FileSpec", 6));
        }
    }
}
