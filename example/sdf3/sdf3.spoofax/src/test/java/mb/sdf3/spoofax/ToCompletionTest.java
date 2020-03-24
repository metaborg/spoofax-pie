package mb.sdf3.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3ToCompletion;
import mb.sdf3.spoofax.task.Sdf3ToCompletionRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToCompletionTest extends TestBase {
    @Test void testTask() throws ExecException {
        final TextResource resource = textResourceRegistry.createResource("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToCompletion taskDef = languageComponent.getToCompletion();
        try(final MixedSession session = languageComponent.newPieSession()) {
            final @Nullable IStrategoTerm output = session.require(taskDef.createTask(languageComponent.getParse().createAstSupplier(resource.key)));
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isApplAt(output, 0, "Unparameterized"));
            assertTrue(isStringAt(output.getSubterm(0), 0, "completion/nested/a-completion-insertions"));
        }
    }
}
