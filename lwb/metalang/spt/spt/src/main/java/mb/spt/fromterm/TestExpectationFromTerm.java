package mb.spt.fromterm;

import mb.common.region.Region;
import mb.common.util.SetView;
import mb.resource.ResourceKey;
import mb.spt.model.TestExpectation;
import mb.spt.resource.SptTestCaseResourceRegistry;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.terms.TermFactory;

import java.util.HashSet;

public interface TestExpectationFromTerm {
    SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory);

    TestExpectation convert(
        IStrategoAppl term,
        Region fallbackRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) throws FromTermException;
}
