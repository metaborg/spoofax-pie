package mb.sdf3.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
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
        final TextResource resourceMain = createTextResource("module test imports lex nested/a nested/b context-free start-symbols Start context-free syntax Start.Start = <<A> <B>>", "test.sdf3");
        final TextResource resourceLex = createTextResource("module lex lexical syntax LAYOUT = [\\ \\t\\n\\r]", "lex.sdf3");
        final TextResource resourceA = createTextResource("module nested/a context-free syntax A.A = <key>", "nested/a.sdf3");
        final TextResource resourceB = createTextResource("module nested/b context-free syntax B.B = <word>", "nested/b.sdf3");
        final Sdf3SpecToParseTable taskDef = languageComponent.getSpecToParseTable();
        try(final MixedSession session = newSession()) {
            final Sdf3Parse parse = languageComponent.getParse();
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
