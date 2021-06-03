package mb.calc;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ParseTest extends TestBase {
    @Test void testParseTask() throws Exception {
        final FSResource resource = textFile("test.calc", "1 + 2;");
        try(final MixedSession session = newSession()) {
            final Result<JsglrParseOutput, JsglrParseException> result = session.require(component.getCalcParse().createTask(component.getCalcParse().inputBuilder().withFile(resource.getKey()).build()));
            assertTrue(result.isOk());
            final JsglrParseOutput output = result.unwrap();
            final IStrategoTerm ast = output.ast;
            assertNotNull(ast);
            assertTrue(isAppl(ast, "Program", 1));
            assertTrue(isListAt(ast, 0));
        }
    }
}
