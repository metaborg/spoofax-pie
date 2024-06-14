package mb.sdf3.adapter;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.resource.text.TextResource;
import mb.sdf3.task.Sdf3ToNormalForm;

import org.junit.jupiter.api.Test;
import org.metaborg.util.tuple.Tuple2;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;

import static mb.sdf3.task.util.Sdf3StrategoTransformTaskDef.inputSupplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.spoofax.terms.util.TermUtils.*;

class ToNormalFormTest extends TestBase {
    @Test void testTask() throws Exception {
        final TextResource resource = textResource("a.sdf3", "module nested/a context-free syntax A = <A>");
        final Sdf3ToNormalForm taskDef = component.getSdf3ToNormalForm();
        try(final MixedSession session = newSession()) {
            final Result<IStrategoTerm, ?> result = session.require(taskDef.createTask(inputSupplier(desugarSupplier(resource),
                Option.ofSome(Tuple2.of("$", "")))));
            assertTrue(result.isOk());
            final IStrategoTerm output = result.unwrap();
            log.info("{}", output);
            assertNotNull(output);
            assertTrue(isAppl(output, "Module"));
            assertTrue(isApplAt(output, 0, "Unparameterized"));
            assertTrue(isStringAt(output.getSubterm(0), 0, "normalized/nested/a-norm"));

            assertTrue(isListAt(output, 2));
            IStrategoList sections = toListAt(output, 2);
            assertTrue(isApplAt(sections, 0, "SDFSection"));
            IStrategoAppl section = toApplAt(sections, 0);
            assertTrue(isApplAt(section, 0, "Kernel"));
            IStrategoAppl kernel = toApplAt(section, 0);
            assertTrue(isListAt(kernel, 0));
            IStrategoList productions = toListAt(kernel, 0);
            assertTrue(isApplAt(productions, 1, "SdfProduction"));
            IStrategoAppl production = toApplAt(productions, 1);
            assertTrue(isApplAt(production, 0, "Lit"));
            IStrategoAppl lit = toApplAt(production, 0);
            assertTrue(isStringAt(lit, 0, "\"$A\""));
        }
    }
}
