package mb.sdf3.spoofax;

import mb.common.util.ListView;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3SpecToParenthesizer;
import mb.sdf3.spoofax.task.Sdf3SpecToParseTable;
import org.junit.jupiter.api.Test;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class SpecToParenthesizerTest extends TestBase {
    @Test void testTask() throws ExecException {
        final TextResource resource = createResource("module test context-free syntax A = <A>", "a.sdf3");
        final Sdf3SpecToParenthesizer taskDef = languageComponent.getSpecToParenthesizer();
        try(final MixedSession session = languageComponent.newPieSession()) {
            final Sdf3SpecToParseTable.Args parseTableArgs = new Sdf3SpecToParseTable.Args(
                desugaredAstSupplier(resource),
                ListView.of(),
                new ParseTableConfiguration(false, false, true, false, false),
                false
            );
            final Sdf3SpecToParenthesizer.Args parenthesizerArgs = new Sdf3SpecToParenthesizer.Args(
                languageComponent.getSpecToParseTable().createSupplier(parseTableArgs),
                "test"
            );
            final @Nullable IStrategoTerm output = session.require(taskDef.createTask(parenthesizerArgs));
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "pp/test-parenthesize"));
        }
    }
}
