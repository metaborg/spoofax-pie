package mb.tiger;

import mb.jsglr.common.JsglrParseOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TigerParserTest extends TestBase {
    @Test void parse() throws Exception {
        final JsglrParseOutput parsed = parse("1");
        assertFalse(parsed.recovered);
        assertEquals(parsed.ast, termFactory.makeAppl(termFactory.makeConstructor("Mod", 1),
            termFactory.makeAppl(termFactory.makeConstructor("Int", 1), termFactory.makeString("1"))));
    }
}
