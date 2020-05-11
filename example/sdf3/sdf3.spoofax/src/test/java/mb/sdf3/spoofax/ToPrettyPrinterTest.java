package mb.sdf3.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3ToPrettyPrinter;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToPrettyPrinterTest extends TestBase {
    @Test void testTask() throws ExecException {
        final TextResource resource = createTextResource("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToPrettyPrinter taskDef = languageComponent.getToPrettyPrinter();
        try(final MixedSession session = newSession()) {
            final @Nullable IStrategoTerm output = session.require(taskDef.createTask(desugarSupplier(resource)));
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "pp/nested/a-pp"));
        }
    }
}
