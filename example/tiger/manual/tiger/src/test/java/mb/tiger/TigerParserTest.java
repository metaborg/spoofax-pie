package mb.tiger;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TigerParserTest extends TigerTestBase {
    @Test void parse() throws InterruptedException {
        final Result<JSGLR1ParseOutput, JSGLR1ParseException> result = parser.parse("1", "Module");
        assertTrue(result.isOk());
        final JSGLR1ParseOutput output = result.unwrapUnchecked();
        assertFalse(output.recovered);
        assertEquals(output.ast, termFactory.makeAppl(termFactory.makeConstructor("Mod", 1),
            termFactory.makeAppl(termFactory.makeConstructor("Int", 1), termFactory.makeString("1"))));
    }
}
