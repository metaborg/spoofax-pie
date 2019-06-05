package mb.stlcrec;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1ParseTableException;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class STLCRecParserTest {
    private final STLCRecParser parser = new STLCRecParser(STLCRecParseTable.fromClassLoaderResources());
    private final ITermFactory termFactory = new TermFactory();

    STLCRecParserTest() throws IOException, JSGLR1ParseTableException {}

    @Test void parse() throws InterruptedException {
        final JSGLR1ParseResult output = parser.parse("1", "Start");
        assertFalse(output.recovered);
        assertNotNull(output.ast);
        assertNotNull(output.tokens);
        assertEquals(output.ast,
            termFactory.makeAppl(termFactory.makeConstructor("Num", 1), termFactory.makeString("1")));
        assertNotNull(output.tokens);
        assertTrue(output.messages.isEmpty());
    }
}
