package mb.calc.spoofax;

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
        final FSResource resource = createTextFile("1 + 2;", "test.calc");
        try(final MixedSession session = newSession()) {
            final Result<JSGLR1ParseOutput, JSGLR1ParseException> result = session.require(languageComponent.getCalcParse().createTask(resourceStringSupplier(resource)));
            assertTrue(result.isOk());
            final JSGLR1ParseOutput output = result.unwrap();
            log.info("{}", output);
            final IStrategoTerm ast = output.ast;
            assertNotNull(ast);
            assertTrue(isAppl(ast, "Program", 1));
            assertTrue(isListAt(ast, 0));
        }
    }
}
