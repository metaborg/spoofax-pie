package mb.spt.expectation;

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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.HashSet;

public class ResolveExpectationFromTerm implements TestExpectationFromTerm {
    @Override
    public SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory) {
        return SetView.of(
            termFactory.makeConstructor("ResolveTo", 2),
            termFactory.makeConstructor("Resolve", 1)
        );
    }

    @Override
    public TestExpectation convert(IStrategoAppl term, Region fallbackRegion, String testSuiteDescription, ResourceKey testSuiteFile, SptTestCaseResourceRegistry testCaseResourceRegistry, HashSet<String> usedResourceNames) throws FromTermException {
        final @Nullable Region expectationSourceRegion = TermTracer.getRegion(term);
        final Region sourceRegion = expectationSourceRegion != null ? expectationSourceRegion : fallbackRegion;
        final IStrategoConstructor constructor = term.getConstructor();
        switch(constructor.getName()) {
            case "Resolve":
                return convertResolveExpectation(term, fallbackRegion, sourceRegion);
            case "ResolveTo":
                return convertResolveToExpectation(term, fallbackRegion, sourceRegion);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a resolve expectation;" +
                    " term is not a valid resolve expectation (no matching constructor)");
        }
    }

    private TestExpectation convertResolveExpectation(IStrategoAppl term, Region fallbackRegion, Region sourceRegion) {
        final SelectionReference fromRef = convertSelectionReference(term, fallbackRegion, 0, "int as first subterm");
        return new ResolveExpectation(fromRef, sourceRegion);
    }

    private TestExpectation convertResolveToExpectation(IStrategoAppl term, Region fallbackRegion, Region sourceRegion) {
        final SelectionReference fromRef = convertSelectionReference(term, fallbackRegion, 0, "int as first subterm");
        final SelectionReference toRef = convertSelectionReference(term, fallbackRegion, 1, "int as second subterm");
        return new ResolveToExpectation(fromRef, toRef, sourceRegion);
    }

    private SelectionReference convertSelectionReference(IStrategoAppl term, Region fallbackRegion, int index, String expected) {
        final IStrategoInt fromTerm = TermUtils.asIntAt(term, index)
            .orElseThrow(() -> new InvalidAstShapeException(expected, term));
        final @Nullable Region fromRegion = TermTracer.getRegion(fromTerm);
        return new SelectionReference(fromTerm.intValue(), fromRegion != null ? fromRegion : fallbackRegion);
    }
}
