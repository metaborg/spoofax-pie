package mb.spt.expectation;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.spt.api.parse.ExpectedParseResult;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.api.model.TestExpectation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;

public class ParseExpectationFromTerm implements TestExpectationFromTerm {
    @Override public SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory) {
        return SetView.of(
            termFactory.makeConstructor("ParseSucceeds", 0),
            termFactory.makeConstructor("ParseFails", 0),
            termFactory.makeConstructor("ParseAmbiguous", 0)
        );
    }

    @Override public TestExpectation convert(IStrategoAppl term, Region fallbackRegion) throws FromTermException {
        final ExpectedParseResult expected = convertParseResult(term);
        final @Nullable Region sourceRegion = TermTracer.getRegion(term);
        return new ParseExpectation(expected, sourceRegion != null ? sourceRegion : fallbackRegion);
    }

    private ExpectedParseResult convertParseResult(IStrategoAppl term) throws FromTermException {
        final IStrategoConstructor constructor = term.getConstructor();
        if(constructor.getArity() > 0) {
            throw new FromTermException("Cannot convert term '" + term + "' to a parse expectation; term is not a valid parse expectation (arity > 0)");
        }
        switch(constructor.getName()) {
            case "ParseSucceeds":
                return new ExpectedParseResult(true, Option.ofNone(), Option.ofNone());
            case "ParseFails":
                return new ExpectedParseResult(false, Option.ofNone(), Option.ofNone());
            case "ParseAmbiguous":
                return new ExpectedParseResult(true, Option.ofNone(), Option.ofSome(true));
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a parse expectation; term is not a valid parse expectation (no matching constructor)");
        }
    }
}
