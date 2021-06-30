package mb.spt.expectation;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.spt.api.model.TestExpectation;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.fromterm.TestExpectationFromTerm;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

public class ParseExpectationsFromTerm implements TestExpectationFromTerm {
    @Override public SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory) {
        return SetView.of(
            termFactory.makeConstructor("ParseSucceeds", 0),
            termFactory.makeConstructor("ParseFails", 0),
            termFactory.makeConstructor("ParseAmbiguous", 0),
            termFactory.makeConstructor("ParseToAterm", 1),
            termFactory.makeConstructor("ParseTo", 1)
        );
    }

    @Override public TestExpectation convert(IStrategoAppl term, Region fallbackRegion) throws FromTermException {
        final @Nullable Region expectationSourceRegion = TermTracer.getRegion(term);
        final Region sourceRegion = expectationSourceRegion != null ? expectationSourceRegion : fallbackRegion;
        final IStrategoConstructor constructor = term.getConstructor();
        switch(constructor.getName()) {
            case "ParseSucceeds":
                return new ParseExpectation(true, Option.ofNone(), Option.ofNone(), sourceRegion);
            case "ParseFails":
                return new ParseExpectation(false, Option.ofNone(), Option.ofNone(), sourceRegion);
            case "ParseAmbiguous":
                return new ParseExpectation(true, Option.ofNone(), Option.ofSome(true), sourceRegion);
            case "ParseToAterm":
                return convertParseToAtermExpectation(term, sourceRegion);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a parse expectation; term is not a valid parse expectation (no matching constructor)");
        }
    }

    private ParseToAtermExpectation convertParseToAtermExpectation(IStrategoAppl appl, Region sourceRegion) {
        final IStrategoAppl toAterm = TermUtils.asApplAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", appl));
        if(!TermUtils.isAppl(toAterm, "ToAterm", 1)) {
            throw new InvalidAstShapeException("ToAterm/1 term application", toAterm);
        }
        final IStrategoAppl aterm = TermUtils.asApplAt(toAterm, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", toAterm));
        return new ParseToAtermExpectation(aterm, sourceRegion);
    }
}
