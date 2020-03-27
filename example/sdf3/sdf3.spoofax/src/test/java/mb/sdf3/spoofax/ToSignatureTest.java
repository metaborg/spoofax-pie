package mb.sdf3.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3ToSignature;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToSignatureTest extends TestBase {
    @Test void testTask() throws ExecException {
        final TextResource resource = createResource("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToSignature taskDef = languageComponent.getToSignature();
        try(final MixedSession session = languageComponent.newPieSession()) {
            final @Nullable IStrategoTerm output = session.require(taskDef.createTask(desugaredAstSupplier(resource)));
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "signatures/nested/a-sig"));
        }
    }
}
