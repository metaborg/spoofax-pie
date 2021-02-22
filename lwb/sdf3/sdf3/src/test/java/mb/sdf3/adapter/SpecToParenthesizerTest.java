package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.task.Sdf3ParseTableToParenthesizer;
import mb.sdf3.task.Sdf3SpecToParseTable;
import org.junit.jupiter.api.Test;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class SpecToParenthesizerTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = textResource("a.sdf3", "module test context-free syntax A = <A>");
        final Sdf3ParseTableToParenthesizer taskDef = component.getSdf3ParseTableToParenthesizer();
        try(final MixedSession session = newSession()) {
            final Sdf3SpecToParseTable.Args parseTableArgs = new Sdf3SpecToParseTable.Args(
                specSupplier(desugarSupplier(resource)),
                new ParseTableConfiguration(false, false, true, false, false, false),
                false
            );
            final Sdf3ParseTableToParenthesizer.Args parenthesizerArgs = new Sdf3ParseTableToParenthesizer.Args(
                component.getSdf3SpecToParseTable().createSupplier(parseTableArgs),
                "test"
            );
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(parenthesizerArgs));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "pp/test-parenthesize"));
        }
    }
}
