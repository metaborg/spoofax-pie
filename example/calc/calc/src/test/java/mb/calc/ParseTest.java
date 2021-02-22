package mb.calc;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
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
            final Result<JSGLR1ParseOutput, JSGLR1ParseException> result = session.require(component.getCalcParse().createTask(resourceStringSupplier(resource)));
            assertTrue(result.isOk());
            final JSGLR1ParseOutput output = result.unwrap();
            final IStrategoTerm ast = output.ast;
            assertNotNull(ast);
            assertTrue(isAppl(ast, "Program", 1));
            assertTrue(isListAt(ast, 0));
        }
    }
}
