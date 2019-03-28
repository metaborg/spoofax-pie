package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.common.JSGLR1ParseTableException;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private final Parser parser = new Parser(ParseTable.fromClassLoaderResources());
    private final ITermFactory termFactory = new TermFactory();

    ParserTest() throws IOException, JSGLR1ParseTableException {}

    @Test void parse() throws InterruptedException {
        final JSGLR1ParseOutput output = parser.parse("1", "Module");
        assertFalse(output.recovered);
        assertNotNull(output.ast);
        assertNotNull(output.tokens);
        assertEquals(output.ast, termFactory.makeAppl(termFactory.makeConstructor("Mod", 1),
            termFactory.makeAppl(termFactory.makeConstructor("Int", 1), termFactory.makeString("1"))));
        assertNotNull(output.tokens);
        assertTrue(output.messages.isEmpty());
    }
}
