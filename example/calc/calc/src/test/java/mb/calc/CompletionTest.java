package mb.calc;

import mb.stratego.common.StrategoRuntime;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class CompletionTest extends TestBase {
    @Test void testCompletion() throws Exception {
        final StrategoRuntime strategoRuntime = component.getStrategoRuntimeProvider().get();
        final ITermFactory tf = strategoRuntime.getTermFactory();
        final IStrategoTerm input = tf.makeTuple(tf.makeString("Program"), tf.makeAppl(tf.makeConstructor("Program-Plhdr", 0)));
        final IStrategoTerm completionTerm = strategoRuntime.invoke("get-proposals-empty-program-calc", input);
        final IStrategoTerm firstCompletion = toTupleAt(completionTerm, 0);
        assertEquals("Program", toJavaStringAt(firstCompletion, 0));
        assertEquals("[[Stat]]", toJavaStringAt(firstCompletion, 1));
    }
}
