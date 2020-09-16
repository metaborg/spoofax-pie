package mb.sdf3.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3ParseTableToParenthesizer;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import org.junit.jupiter.api.Test;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class SpecToParenthesizerTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = createTextResource("module test context-free syntax A = <A>", "a.sdf3");
        final Sdf3ParseTableToParenthesizer taskDef = languageComponent.getSpecToParenthesizer();
        try(final MixedSession session = newSession()) {
            final Sdf3SpecToParseTable.Args parseTableArgs = new Sdf3SpecToParseTable.Args(
                specSupplier(desugarSupplier(resource)),
                new ParseTableConfiguration(false, false, true, false, false, false),
                false
            );
            final Sdf3ParseTableToParenthesizer.Args parenthesizerArgs = new Sdf3ParseTableToParenthesizer.Args(
                languageComponent.getSpecToParseTable().createSupplier(parseTableArgs),
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
