package mb.spt.util;

import mb.spt.fromterm.InvalidAstShapeException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.Optional;

public class SptFromTermUtil {
    public static Optional<IStrategoTerm> getOptional(IStrategoTerm term) throws InvalidAstShapeException {
        final IStrategoAppl appl = TermUtils.asAppl(term)
            .orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        if(appl.getConstructor().getName().equals("None")) {
            return Optional.empty();
        } else if(appl.getSubtermCount() == 1) {
            return Optional.of(appl.getSubterm(0));
        } else {
            throw new InvalidAstShapeException("a Some term with one subterm", appl);
        }
    }
}
