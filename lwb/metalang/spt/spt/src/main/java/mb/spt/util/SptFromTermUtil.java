package mb.spt.util;

import mb.common.option.Option;
import mb.spt.fromterm.InvalidAstShapeException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class SptFromTermUtil {
    public static Option<IStrategoTerm> getOptional(IStrategoTerm term) throws InvalidAstShapeException {
        final IStrategoAppl appl = TermUtils.asAppl(term)
            .orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        if(appl.getConstructor().getName().equals("None")) {
            return Option.ofNone();
        } else if(appl.getSubtermCount() == 1) {
            return Option.ofSome(appl.getSubterm(0));
        } else {
            throw new InvalidAstShapeException("a Some term with one subterm", appl);
        }
    }
}
