package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.task.Sdf3SpecToParseTable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

class SpecToParseTableTest extends TestBase {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testTask(boolean createCompletionTable) throws Exception {
        final TextResource resourceMain = textResource("test.sdf3", "module test imports lex nested/a nested/b context-free start-symbols Start context-free syntax Start.Start = <<A> <B>>");
        final TextResource resourceLex = textResource("lex.sdf3", "module lex lexical syntax LAYOUT = [\\ \\t\\n\\r]");
        final TextResource resourceA = textResource("nested/a.sdf3", "module nested/a context-free syntax A.A = <key>");
        final TextResource resourceB = textResource("nested/b.sdf3", "module nested/b context-free syntax B.B = <word>");
        final Sdf3SpecToParseTable taskDef = component.getSdf3SpecToParseTable();
        try(final MixedSession session = newSession()) {
            final Sdf3SpecToParseTable.Args args = new Sdf3SpecToParseTable.Args(
                specSupplier(desugarSupplier(resourceMain), desugarSupplier(resourceLex), desugarSupplier(resourceA), desugarSupplier(resourceB)),
                new ParseTableConfiguration(false, false, true, false, false, false),
                createCompletionTable
            );
            final Result<ParseTable, ?> parseTableResult = session.require(taskDef.createTask(args));
            assertTrue(parseTableResult.isOk());
            final ParseTable parseTable = parseTableResult.unwrap();
            assertNotNull(parseTable);
            final JSGLR2<IStrategoTerm> parser = JSGLR2Variant.Preset.standard.getJSGLR2(parseTable);
            {
                final JSGLR2Result<IStrategoTerm> parseResult = parser.parseResult("key word", "", "Start");
                for(Message message : parseResult.messages) {
                    log.error("Parse message: {}", message.message);
                }
                assertTrue(parseResult.isSuccess());
                final IStrategoTerm ast = ((JSGLR2Success<IStrategoTerm>)parseResult).ast;
                assertTrue(isAppl(ast, "Start", 2));
                assertTrue(isApplAt(ast, 0, "A", 0));
                assertTrue(isApplAt(ast, 1, "B", 0));
            }
            if(createCompletionTable) {
                final JSGLR2Result<IStrategoTerm> parseResult = parser.parseResult("$A $B", "", "Start");
            }
        }
    }
}
