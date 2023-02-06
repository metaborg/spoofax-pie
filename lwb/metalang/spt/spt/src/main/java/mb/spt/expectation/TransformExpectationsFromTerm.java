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

public class TransformExpectationsFromTerm implements TestExpectationFromTerm {
    @Override public SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory) {
        return SetView.of(
            termFactory.makeConstructor("Transform", 1),
            termFactory.makeConstructor("Transform", 2),
            termFactory.makeConstructor("TransformToAterm", 2)
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
            case "Transform":
                switch(constructor.getArity()) {
                    case 1:
                        return convertToExpectation(term, sourceRegion);
                    case 2:
                        return convertToFragmentExpectation(term, sourceRegion, testSuiteDescription, testSuiteFile, testCaseResourceRegistry, usedResourceNames);
                }
            case "TransformToAterm":
                return convertToAtermExpectation(term, sourceRegion);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a transform expectation; term is not a valid transform expectation (no matching constructor)");
        }
    }

    private TransformExpectation convertToExpectation(IStrategoAppl appl, Region sourceRegion) {
        final String commandDisplayName = TermUtils.asJavaStringAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", appl));
        return new TransformExpectation(tryRemoveDoubleQuotes(commandDisplayName), sourceRegion);
    }

    private TransformToAtermExpectation convertToAtermExpectation(IStrategoAppl appl, Region sourceRegion) {
        final String commandDisplayName = TermUtils.asJavaStringAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", appl));
        final IStrategoAppl toAterm = TermUtils.asApplAt(appl, 1)
            .orElseThrow(() -> new InvalidAstShapeException("term application second first subterm", appl));
        if(!TermUtils.isAppl(toAterm, "ToAterm", 1)) {
            throw new InvalidAstShapeException("ToAterm/1 term application", toAterm);
        }
        final IStrategoAppl aterm = TermUtils.asApplAt(toAterm, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", toAterm));
        return new TransformToAtermExpectation(aterm, tryRemoveDoubleQuotes(commandDisplayName), sourceRegion);
    }

    private TransformToFragmentExpectation convertToFragmentExpectation(
        IStrategoAppl appl,
        Region sourceRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) {
        final String commandDisplayName = TermUtils.asJavaStringAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", appl));
        final IStrategoAppl toPart = TermUtils.asApplAt(appl, 1)
            .orElseThrow(() -> new InvalidAstShapeException("term application as second subterm", appl));
        if(!TermUtils.isAppl(toPart, "ToPart", 4)) {
            throw new InvalidAstShapeException("ToPart/4 term application", toPart);
        }
        final Option<CoordinateRequirement> languageCoordinateRequirementHint = SptFromTermUtil.getOptional(toPart.getSubterm(0))
            .map(t -> TermUtils.asJavaString(t).orElseThrow(() -> new InvalidAstShapeException("term string", t)))
            .flatMap(CoordinateRequirement::parse);
        final TestFragmentImpl fragment = TestSuiteFromTerm.fragmentFromTerm(toPart.getSubterm(2), null);
        final String resourceName = TestSuiteFromTerm.getResourceName(usedResourceNames, testSuiteDescription);
        final SptTestCaseResource resource = testCaseResourceRegistry.registerTestCase(testSuiteFile, resourceName, fragment.asText());
        return new TransformToFragmentExpectation(resource.getPath(), languageCoordinateRequirementHint.get(), tryRemoveDoubleQuotes(commandDisplayName), sourceRegion);
    }

    public static String tryRemoveDoubleQuotes(String string) {
        if(string.startsWith("\"")) {
            string = string.substring(1);
        }
        if(string.endsWith("\"")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }
}
