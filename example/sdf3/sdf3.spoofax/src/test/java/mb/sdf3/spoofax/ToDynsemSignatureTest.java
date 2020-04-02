package mb.sdf3.spoofax;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.constraint.common.ConstraintAnalyzer;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Supplier;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import mb.sdf3.spoofax.task.Sdf3AnalyzeMulti;
import mb.sdf3.spoofax.task.Sdf3ToDynsemSignature;
import mb.sdf3.spoofax.task.SingleFileAnalysisResult;
import mb.sdf3.spoofax.task.StrategoTransformAnalyzedTaskDef;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToDynsemSignatureTest extends TestBase {
    @Test void testTask() throws ExecException, IOException {
        final FSResource resource = createTextFile("module nested/a context-free syntax A = <A>", "a.sdf3");
        final Sdf3ToDynsemSignature taskDef = languageComponent.getToDynsemSignature();
        try(final MixedSession session = languageComponent.newPieSession()) {
            final Supplier<SingleFileAnalysisResult> supplier = singleFileAnalysisResultSupplier(resource);
            final @Nullable IStrategoTerm output = session.require(taskDef.createTask(supplier));
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isStringAt(output, 0, "ds-signatures/nested/a-sig"));
        }
    }
}
