package mb.str;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.str.util.TestBase;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ParseTest extends TestBase {
    @Test void testParseTask() throws Exception {
        final FSResource resource = textFile("a.str", "module a");
        try(final MixedSession session = newSession()) {
            final Result<JsglrParseOutput, JsglrParseException> result = session.require(parse.createTask(parse.inputBuilder().withFile(resource.getKey()).build()));
            assertTrue(result.isOk());
            final JsglrParseOutput output = result.unwrap();
            log.info("{}", output);
            final IStrategoTerm ast = output.ast;
            assertNotNull(ast);
            assertTrue(isAppl(ast, "Module"));
            assertTrue(isStringAt(ast, 0, "a"));
        }
    }
}
