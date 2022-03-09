package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.resource.text.TextResource;
import mb.sdf3.task.spec.Sdf3Config;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metaborg.sdf2table.parsetable.ParseTable;
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
        textFile("src/start.sdf3", "module test imports lex nested/a nested/b context-free start-symbols Start context-free syntax Start.Start = <<A> <B>>");
        textFile("src/lex.sdf3", "module lex lexical syntax LAYOUT = [\\ \\t\\n\\r]");
        textFile("src/nested/a.sdf3", "module nested/a context-free syntax A.A = <key>");
        textFile("src/nested/b.sdf3", "module nested/b context-free syntax B.B = <word>");
        final Sdf3SpecToParseTable taskDef = component.getSdf3SpecToParseTable();
        final Sdf3SpecConfig sdf3SpecConfig = specConfig(rootDirectory.getPath());
        final Sdf3Config sdf3Config = new Sdf3Config("$", "");
        final String strategyAffix = "lang";
        try(final MixedSession session = newSession()) {
            final Sdf3SpecToParseTable.Input input = new Sdf3SpecToParseTable.Input(
                sdf3SpecConfig,
                sdf3Config,
                strategyAffix,
                createCompletionTable
            );
            final Result<ParseTable, ?> parseTableResult = session.require(taskDef.createTask(input));
            assertTrue(parseTableResult.isOk(), () -> new ExceptionPrinter().printExceptionToString(parseTableResult.getErr()));
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
