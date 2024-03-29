package mb.tiger;

import mb.jsglr.common.JsglrParseOutput;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import static org.junit.jupiter.api.Assertions.*;

class TigerStrategoRuntimeBuilderTest extends TestBase {
    @Test void parseUnparse() throws Exception {
        final String str = "1 + 2";
        final JsglrParseOutput parsed = parse(str);
        final @Nullable IStrategoTerm unparsedTerm = strategoRuntime.invoke("pp-Tiger-string", parsed.ast);
        assertNotNull(unparsedTerm);
        final IStrategoString unparsedStringTerm = (IStrategoString)unparsedTerm;
        final String unparsed = unparsedStringTerm.stringValue();
        assertEquals(str, unparsed);
    }
}
