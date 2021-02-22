package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.Supplier;
import mb.resource.fs.FSResource;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3ToSignature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToSignatureTest extends TestBase {
    @Test
    @Disabled("For some reason, SingleLineTemplate are not getting a type annotation on the AST, even though there is a Statix type rule for them")
    void testTask() throws Exception {
        final FSResource resource = textFile("a.sdf3", "module nested/a context-free syntax A = <A>");
        final Sdf3ToSignature taskDef = component.getSdf3ToSignature();
        try(final MixedSession session = newSession()) {
            final Supplier<? extends Result<Sdf3AnalyzeMulti.SingleFileOutput, ?>> supplier = singleFileAnalysisResultSupplier(resource);
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(supplier));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "signatures/nested/a-sig"));
        }
    }
}
