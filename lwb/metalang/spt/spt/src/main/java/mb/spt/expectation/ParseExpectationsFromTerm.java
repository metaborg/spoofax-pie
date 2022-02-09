package mb.spt.expectation;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.spoofax.core.CoordinateRequirement;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.fromterm.TestSuiteFromTerm;
import mb.spt.model.TestExpectation;
import mb.spt.model.TestFragmentImpl;
import mb.spt.resource.SptTestCaseResource;
import mb.spt.resource.SptTestCaseResourceRegistry;
import mb.spt.util.SptFromTermUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.HashSet;
import java.util.Optional;

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

    @Override public TestExpectation convert(
        IStrategoAppl term,
        Region fallbackRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) throws FromTermException {
        final @Nullable Region expectationSourceRegion = TermTracer.getRegion(term);
        final Region sourceRegion = expectationSourceRegion != null ? expectationSourceRegion : fallbackRegion;
        final IStrategoConstructor constructor = term.getConstructor();
        switch(constructor.getName()) {
            case "ParseSucceeds":
                return new ParseExpectation(true, ParseExpectation.Ambiguity.Unambiguous, ParseExpectation.Recovery.NotRecovered, sourceRegion);
            case "ParseFails":
                return new ParseExpectation(false, ParseExpectation.Ambiguity.DoNotCare, ParseExpectation.Recovery.DoNotCare, sourceRegion);
            case "ParseAmbiguous":
                return new ParseExpectation(true, ParseExpectation.Ambiguity.Ambiguous, ParseExpectation.Recovery.DoNotCare, sourceRegion);
            case "ParseToAterm":
                return convertToAtermExpectation(term, sourceRegion);
            case "ParseTo":
                return convertToFragmentExpectation(term, sourceRegion, testSuiteDescription, testSuiteFile, testCaseResourceRegistry, usedResourceNames);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a parse expectation; term is not a valid parse expectation (no matching constructor)");
        }
    }

    private ParseToAtermExpectation convertToAtermExpectation(IStrategoAppl appl, Region sourceRegion) {
        final IStrategoAppl toAterm = TermUtils.asApplAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", appl));
        if(!TermUtils.isAppl(toAterm, "ToAterm", 1)) {
            throw new InvalidAstShapeException("ToAterm/1 term application", toAterm);
        }
        final IStrategoAppl aterm = TermUtils.asApplAt(toAterm, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", toAterm));
        return new ParseToAtermExpectation(aterm, sourceRegion);
    }

    private ParseToFragmentExpectation convertToFragmentExpectation(
        IStrategoAppl appl,
        Region sourceRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) {
        final IStrategoAppl toPart = TermUtils.asApplAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", appl));
        if(!TermUtils.isAppl(toPart, "ToPart", 4)) {
            throw new InvalidAstShapeException("ToPart/4 term application", toPart);
        }
        final Option<CoordinateRequirement> languageCoordinateRequirementHint = SptFromTermUtil.getOptional(toPart.getSubterm(0))
            .map(t -> TermUtils.asJavaString(t).orElseThrow(() -> new InvalidAstShapeException("term string", t)))
            .flatMap(CoordinateRequirement::parse);
        final TestFragmentImpl fragment = TestSuiteFromTerm.fragmentFromTerm(toPart.getSubterm(2), null);
        final String resourceName = TestSuiteFromTerm.getResourceName(usedResourceNames, testSuiteDescription);
        final SptTestCaseResource resource = testCaseResourceRegistry.registerTestCase(testSuiteFile, resourceName, fragment.asText());
        return new ParseToFragmentExpectation(resource.getKey(), languageCoordinateRequirementHint.get(), sourceRegion);
    }
}
