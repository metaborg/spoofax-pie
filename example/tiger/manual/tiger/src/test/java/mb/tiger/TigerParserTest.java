package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TigerParserTest extends TigerTestBase {


    @Test void parse() throws InterruptedException {
        final JSGLR1ParseResult result = parser.parse("1", "Module");
        assertTrue(result.hasSucceeded());
        assertFalse(result.hasRecovered());
        assertFalse(result.hasFailed());
        assertTrue(result.getAst().isPresent());
        assertTrue(result.getTokens().isPresent());
        assertEquals(result.getAst().get(), termFactory.makeAppl(termFactory.makeConstructor("Mod", 1),
            termFactory.makeAppl(termFactory.makeConstructor("Int", 1), termFactory.makeString("1"))));
        assertTrue(result.getMessages().isEmpty());
    }
}
