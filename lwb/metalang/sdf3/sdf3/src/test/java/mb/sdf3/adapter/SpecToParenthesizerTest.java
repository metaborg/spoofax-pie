package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.sdf3.task.spec.Sdf3ParseTableToParenthesizer;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class SpecToParenthesizerTest extends TestBase {
    @Test void testTask() throws Exception {
        final FSResource resource = textFile("src/start.sdf3", "module start context-free syntax A = <A>");
        final Sdf3ParseTableToParenthesizer taskDef = component.getSdf3ParseTableToParenthesizer();
        try(final MixedSession session = newSession()) {
            final Sdf3SpecToParseTable.Input parseTableInput = new Sdf3SpecToParseTable.Input(
                specConfig(rootDirectory.getPath(), directory("src").getPath(), resource.getPath()),
                false
            );
            final Sdf3ParseTableToParenthesizer.Args parenthesizerArgs = new Sdf3ParseTableToParenthesizer.Args(
                component.getSdf3SpecToParseTable().createSupplier(parseTableInput),
                "start"
            );
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(parenthesizerArgs));
            assertTrue(result.isOk(), () -> new ExceptionPrinter().printExceptionToString(result.getErr()));
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "pp/start-parenthesize"));
        }
    }
}
