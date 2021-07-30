package mb.spt.expectation;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestExpectation;
import mb.spt.resource.SptTestCaseResourceRegistry;
import mb.spt.util.SptFromTermUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.HashSet;

public class RunExpectationFromTerm implements TestExpectationFromTerm {
    @Override
    public SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory) {
        return SetView.of(
            termFactory.makeConstructor("Run", 4),
            termFactory.makeConstructor("RunToAterm", 3)
        );
    }

    @Override
    public TestExpectation convert(
        IStrategoAppl term,
        Region fallbackRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) throws FromTermException {
        final @Nullable Region termSourceRegion = TermTracer.getRegion(term);
        final Region sourceRegion = termSourceRegion != null ? termSourceRegion : fallbackRegion;
        final IStrategoConstructor constructor = term.getConstructor();
        switch(constructor.getName()) {
            case "Run":
                return convertToRunExpectation(term, sourceRegion);
            case "RunToAterm":
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a run expectation;" +
                    " term is not a valid run expectation (no matching constructor)");
        }
    }

    private TestExpectation convertToRunExpectation(IStrategoAppl term, Region sourceRegion) {
        final String strategyName = TermUtils.asJavaStringAt(term, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", term));
        // TODO: Strategy arguments as 2nd subterm
        final Option<SelectionReference> selection = convertSelections(
            TermUtils.asApplAt(term, 2)
                .orElseThrow(() -> new InvalidAstShapeException("term application as third subterm", term)),
            sourceRegion);
        final IStrategoAppl fragmentOpt = TermUtils.asApplAt(term, 3)
            .orElseThrow(() -> new InvalidAstShapeException("term application as fourth subterm", term));
        return SptFromTermUtil.getOptional(fragmentOpt)
            .map(fragment -> new RunExpectation(strategyName, selection, sourceRegion))  // TODO: make RunToFragment
            .orElseGet(() -> new RunExpectation(strategyName, selection, sourceRegion));
    }

    private Option<SelectionReference> convertSelections(IStrategoAppl term, Region fallbackRegion) {
        return SptFromTermUtil.getOptional(term)
            .map(subterm -> {
                final int selection = TermUtils.asJavaInt(subterm)
                    .orElseThrow(() -> new InvalidAstShapeException("an integer", subterm));
                final @Nullable Region region = TermTracer.getRegion(term);
                return Option.ofSome(new SelectionReference(selection, region != null ? region : fallbackRegion));
            })
            .orElse(Option.ofNone());
    }
}
