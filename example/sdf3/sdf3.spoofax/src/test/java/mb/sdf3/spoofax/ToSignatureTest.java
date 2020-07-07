package mb.sdf3.spoofax;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Supplier;
import mb.resource.fs.FSResource;
import mb.sdf3.spoofax.task.Sdf3ToSignature;
import mb.sdf3.spoofax.task.SingleFileAnalysisResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToSignatureTest extends TestBase {
    @Test
    @Disabled("For some reason, SingleLineTemplate are not getting a type annotation on the AST, even though there is a Statix type rule for them")
    void testTask() throws ExecException, IOException, InterruptedException {
        final FSResource resource = createTextFile("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToSignature taskDef = languageComponent.getToSignature();
        try(final MixedSession session = newSession()) {
            final Supplier<SingleFileAnalysisResult> supplier = singleFileAnalysisResultSupplier(resource);
            final @Nullable IStrategoTerm output = session.require(taskDef.createTask(supplier));
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "signatures/nested/a-sig"));
        }
    }
}
