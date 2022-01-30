package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.task.Sdf3AstStrategoTransformTaskDef;
import mb.sdf3.task.Sdf3ToNormalForm;
import mb.sdf3.task.spec.Sdf3Config;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToNormalFormTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = textResource("a.sdf3", "module nested/a context-free syntax A = <A>");
        final Sdf3Config sdf3Config = new Sdf3Config("$", "");
        final String strategyAffix = "lang";
        final Sdf3ToNormalForm taskDef = component.getSdf3ToNormalForm();
        try(final MixedSession session = newSession()) {
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(new Sdf3AstStrategoTransformTaskDef.Input(
                desugarSupplier(resource),
                sdf3Config,
                strategyAffix
            )));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isApplAt(output, 0, "Unparameterized"));
            assertTrue(isStringAt(output.getSubterm(0), 0, "normalized/nested/a-norm"));
        }
    }
}
