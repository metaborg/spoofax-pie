package mb.calc;

import mb.calc.task.CalcAnalyze;
import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisTest extends TestBase {
    @Test void testAnalysis() throws Exception {
        final FSResource resource = textFile("test.calc", "1 + 2;");
        try(final MixedSession session = newSession()) {
            final Result<CalcAnalyze.Output, ?> result = session.require(component.getCalcAnalyze().createTask(new CalcAnalyze.Input(resource.getKey(), component.getCalcParse().createAstSupplier(resource.getKey()))));
            assertTrue(result.isOk());
            final CalcAnalyze.Output output = result.unwrap();
            assertFalse(output.result.messages.containsError());
        }
    }
}
