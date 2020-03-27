package mb.sdf3.spoofax;

import mb.common.util.ListView;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToTable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.JSGLR2Result;
import org.spoofax.jsglr2.JSGLR2Success;
import org.spoofax.jsglr2.JSGLR2Variant;
import org.spoofax.jsglr2.messages.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToTableTest extends TestBase {
    @Disabled @Test void testTask() throws ExecException {
        final TextResource resourceMain = textResourceRegistry.createResource("module test imports lex nested/a nested/b context-free start-symbols Start context-free syntax Start.Start = <<A> <B>>", "test.sdf3");
        final TextResource resourceLex = textResourceRegistry.createResource("module lex lexical syntax LAYOUT = [\\ \\t\\n\\r]", "lex.sdf3");
        final TextResource resourceA = textResourceRegistry.createResource("module nested/a context-free syntax A.A = <key>", "nested/a.sdf3");
        final TextResource resourceB = textResourceRegistry.createResource("module nested/b context-free syntax B.B = <word>", "nested/b.sdf3");
        final Sdf3ToTable taskDef = languageComponent.getToTable();
        try(final MixedSession session = languageComponent.newPieSession()) {
            final Sdf3Parse parse = languageComponent.getParse();
            final Sdf3ToTable.Args args = new Sdf3ToTable.Args(
                parse.createAstSupplier(resourceMain.key),
                ListView.of(
                    parse.createAstSupplier(resourceLex.key),
                    parse.createAstSupplier(resourceA.key),
                    parse.createAstSupplier(resourceB.key)
                ),
                new ParseTableConfiguration(false, false, true, false, false)
            );
            final ParseTable parseTable = session.require(taskDef.createTask(args));
            log.info("{}", parseTable);
            assertNotNull(parseTable);
            final JSGLR2<IStrategoTerm> parser = JSGLR2Variant.Preset.standard.getJSGLR2(parseTable);
            final JSGLR2Result<IStrategoTerm> parseResult = parser.parseResult("key word", "", "Start");
            log.info("{}", parseResult);
            for(Message message : parseResult.messages) {
                log.error("{}", message.message);
            }
            assertTrue(parseResult.isSuccess());
            final IStrategoTerm ast = ((JSGLR2Success<IStrategoTerm>)parseResult).ast;
            assertTrue(isAppl(ast, "Start", 1));
            assertTrue(isApplAt(ast,0, "A", 0));
            assertTrue(isApplAt(ast,1, "B", 0));
        }
    }
}
