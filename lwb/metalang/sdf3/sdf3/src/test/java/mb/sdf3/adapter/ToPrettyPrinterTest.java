package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.stratego.Sdf3Context;
import mb.sdf3.task.Sdf3ToPrettyPrinter;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToPrettyPrinterTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = textResource("a.sdf3", "module nested/a context-free syntax A = <A>");
        final Sdf3ToPrettyPrinter taskDef = component.getSdf3ToPrettyPrinter();
        try(final MixedSession session = newSession()) {
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(new Sdf3ToPrettyPrinter.Input(desugarSupplier(resource), new Sdf3Context("lang", "$", ""))));
            assertOk(result);
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "pp/nested/a-pp"));
        }
    }
}
