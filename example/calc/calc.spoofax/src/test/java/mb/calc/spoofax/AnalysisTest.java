package mb.calc.spoofax;

import mb.calc.spoofax.task.CalcAnalyze;
import mb.calc.spoofax.task.CalcAnalyzeMulti;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class AnalysisTest extends TestBase {
    @Test void testAnalysis() throws Exception {
        final FSResource resource = createTextFile("1 + 2;", "test.calc");
        try(final MixedSession session = newSession()) {
            final Result<CalcAnalyze.Output, ?> result = session.require(languageComponent.getCalcAnalyze().createTask(new CalcAnalyze.Input(resource.getKey(), languageComponent.getCalcParse().createAstSupplier(resource.getKey()))));
            assertTrue(result.isOk());
            final CalcAnalyze.Output output = result.unwrap();
            assertFalse(output.result.messages.containsError());
        }
    }
}
