package mb.calc;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class TransformTest extends TestBase {
    @Test void testProgramToJava() throws Exception {
        final FSResource resource = textFile("test.calc", "1 + 2;");
        try(final MixedSession session = newSession()) {
            final Result<JsglrParseOutput, JsglrParseException> result = session.require(component.getCalcParse().createTask(component.getCalcParse().inputBuilder().withFile(resource.getKey()).build()));
            assertTrue(result.isOk());
            final IStrategoTerm ast = result.unwrap().ast;
            final StrategoRuntime strategoRuntime = component.getStrategoRuntimeProvider().get();
            final ITermFactory termFactory = strategoRuntime.getTermFactory();
            final IStrategoTerm term = strategoRuntime.invoke("program-to-java", termFactory.makeTuple(ast, termFactory.makeString("Test")));
            assertTrue(isString(term));
            final String str = toJavaString(term);
            assertTrue(str.contains("public class Test"));
            assertTrue(str.contains("new BigDecimal(\"1\").add(new BigDecimal(\"2\"))"));
        }
    }
}
