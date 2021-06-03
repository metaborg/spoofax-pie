package mb.calc;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class PrettyPrintTest extends TestBase {
    @Test void testParsePrettyPrint() throws Exception {
        final String text = "1 + 2 * 3;";
        final FSResource resource = textFile("test.calc", text);
        try(final MixedSession session = newSession()) {
            final Result<JsglrParseOutput, JsglrParseException> result = session.require(component.getCalcParse().createTask(component.getCalcParse().inputBuilder().withFile(resource.getKey()).build()));
            assertTrue(result.isOk());
            final IStrategoTerm ast = result.unwrap().ast;
            final StrategoRuntime strategoRuntime = component.getStrategoRuntimeProvider().get();
            final IStrategoTerm term = strategoRuntime.invoke("pp-calc-string", ast);
            assertTrue(isString(term));
            final String str = toJavaString(term);
            assertEquals(text, str);
        }
    }
}
