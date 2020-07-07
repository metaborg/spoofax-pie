package mb.sdf3.spoofax;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.spoofax.task.Sdf3ToCompletion;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToCompletionTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = createTextResource("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToCompletion taskDef = languageComponent.getToCompletion();
        try(final MixedSession session = newSession()) {
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(desugarSupplier(resource)));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isApplAt(output, 0, "Unparameterized"));
            assertTrue(isStringAt(output.getSubterm(0), 0, "completion/nested/a-completion-insertions"));
        }
    }
}
