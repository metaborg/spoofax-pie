package mb.spt.fromterm;

import mb.common.util.SetView;
import mb.spt.api.model.TestExpectation;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;

public interface TestExpectationFromTerm {
    SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory);

    TestExpectation convert(IStrategoAppl term) throws FromTermException;
}
