package mb.calc;

import mb.calc.task.CalcAnalyze;
import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
