package mb.spt.expectation;

import mb.common.region.Region;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.model.TestExpectation;
import mb.spt.resource.SptTestCaseResourceRegistry;
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
                return convertToExpectation(term, sourceRegion);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a transform expectation; term is not a valid transform expectation (no matching constructor)");
        }
    }

    private TransformExpectation convertToExpectation(IStrategoAppl appl, Region sourceRegion) {
        final String commandDisplayName = TermUtils.asJavaStringAt(appl, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", appl));
        return new TransformExpectation(tryRemoveDoubleQuotes(commandDisplayName), sourceRegion);
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
