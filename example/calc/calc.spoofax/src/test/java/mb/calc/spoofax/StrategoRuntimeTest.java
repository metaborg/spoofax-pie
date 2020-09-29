package mb.calc.spoofax;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.MixedSession;
import mb.resource.fs.FSResource;
import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class StrategoRuntimeTest extends TestBase {
    @Test void testParseTask() throws Exception {
        final FSResource resource = createTextFile("1 + 2;", "test.calc");
        try(final MixedSession session = newSession()) {
            final Result<JSGLR1ParseOutput, JSGLR1ParseException> result = session.require(languageComponent.getCalcParse().createTask(resourceStringSupplier(resource)));
            assertTrue(result.isOk());
            final IStrategoTerm ast = result.unwrap().ast;
            final StrategoRuntime strategoRuntime = languageComponent.getStrategoRuntimeProvider().get();
            final ITermFactory termFactory = strategoRuntime.getTermFactory();
            final IStrategoTerm term = strategoRuntime.invoke("program-to-java", termFactory.makeTuple(ast, termFactory.makeString("Test")));
            assertTrue(isString(term));
            final String str = toJavaString(term);
            assertTrue(str.contains("public class Test"));
            assertTrue(str.contains("new BigDecimal(\"1\").add(new BigDecimal(\"2\"))"));
        }
    }
}
