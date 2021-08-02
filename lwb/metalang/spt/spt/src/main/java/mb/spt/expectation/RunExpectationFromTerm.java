package mb.spt.expectation;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.fromterm.TestSuiteFromTerm;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestExpectation;
import mb.spt.model.TestFragmentImpl;
import mb.spt.resource.SptTestCaseResource;
import mb.spt.resource.SptTestCaseResourceRegistry;
import mb.spt.util.SptFromTermUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

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
                return convertToRunExpectation(term, sourceRegion, testSuiteDescription, testSuiteFile, testCaseResourceRegistry, usedResourceNames);
            case "RunToAterm":
                return convertToRunToAtermExpectation(term, sourceRegion);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a run expectation;" +
                    " term is not a valid run expectation (no matching constructor)");
        }
    }

    private RunToAtermExpectation convertToRunToAtermExpectation(IStrategoAppl term, Region sourceRegion) {
        final String strategyName = TermUtils.asJavaStringAt(term, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", term));
        final Option<SelectionReference> selection = convertSelections(
            TermUtils.asApplAt(term, 1)
                .orElseThrow(() -> new InvalidAstShapeException("term application as second subterm", term)),
            sourceRegion);
        final IStrategoAppl toAterm = TermUtils.asApplAt(term, 2)
            .orElseThrow(() -> new InvalidAstShapeException("term application as third subterm", term));
        if(!TermUtils.isAppl(toAterm, "ToAterm", 1)) {
            throw new InvalidAstShapeException("ToAterm/1 term application", toAterm);
        }
        final IStrategoAppl aterm = TermUtils.asApplAt(toAterm, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term application as first subterm", toAterm));
        return new RunToAtermExpectation(strategyName, selection, aterm, sourceRegion);
    }

    private TestExpectation convertToRunExpectation(
        IStrategoAppl term,
        Region sourceRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) {
        final String strategyName = TermUtils.asJavaStringAt(term, 0)
            .orElseThrow(() -> new InvalidAstShapeException("term string as first subterm", term));
        final Option<ListView<IStrategoAppl>> arguments = convertArguments(
            TermUtils.asApplAt(term, 1)
                .orElseThrow(() -> new InvalidAstShapeException("term application as second subterm", term))
        );
        final Option<SelectionReference> selection = convertSelections(
            TermUtils.asApplAt(term, 2)
                .orElseThrow(() -> new InvalidAstShapeException("term application as third subterm", term)),
            sourceRegion);
        final IStrategoAppl resultExpOpt = TermUtils.asApplAt(term, 3)
            .orElseThrow(() -> new InvalidAstShapeException("term application as fourth subterm", term));
        return SptFromTermUtil.getOptional(resultExpOpt)
            .map(resultExp -> {
                IStrategoAppl resultExpectation = TermUtils.asAppl(resultExp)
                    .orElseThrow(() -> new InvalidAstShapeException("term application as subterm", term));
                switch (resultExpectation.getConstructor().getName()) {
                    case "Fails":
                        return new RunExpectation(strategyName, arguments, selection, sourceRegion, true);
                    case "ToPart":
                        return convertToRunToFragmentExpectation(
                            strategyName,
                            arguments,
                            selection,
                            resultExp,
                            sourceRegion,
                            testSuiteDescription,
                            testSuiteFile,
                            testCaseResourceRegistry,
                            usedResourceNames
                        );
                    default:
                        throw new InvalidAstShapeException("Fails/0 or ToPart/1 as subterm", resultExp);
                }
            })
            .orElseGet(() -> new RunExpectation(strategyName, arguments, selection, sourceRegion, false));
    }

    private Option<ListView<IStrategoAppl>> convertArguments(IStrategoAppl arguments) {
        return SptFromTermUtil.getOptional(arguments).map(
            (args) -> Option.ofSome(ListView.of(
                TermUtils.asJavaListAt(args, 0)
                    .orElseThrow(() -> new InvalidAstShapeException("list", args))
                    .stream()
                    .map(this::convertArgument)
                    .collect(Collectors.toList())
            ))
        ).orElse(Option.ofNone());
    }

    private IStrategoAppl convertArgument(IStrategoTerm term) {
        IStrategoAppl argument = TermUtils.asAppl(term)
            .orElseThrow(() -> new InvalidAstShapeException("term application", term));
        switch(argument.getName()) {
            case "Int":
            case "String":
            case "SelectionRef":
                break;
            default:
                throw new InvalidAstShapeException("Int/1, String/1 or SelectionRef/2", argument);
        }
        if (!TermUtils.isStringAt(argument, 0)) {
            throw new InvalidAstShapeException("string as subterm", argument);
        }
        return argument;
    }

    private TestExpectation convertToRunToFragmentExpectation(
        String strategyName,
        Option<ListView<IStrategoAppl>> arguments,
        Option<SelectionReference> selection,
        IStrategoTerm toPart,
        Region sourceRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) {
        if (!TermUtils.isAppl(toPart, "ToPart", 4)) {
            throw new InvalidAstShapeException("ToPart/4 term application", toPart);
        }
        final Optional<String> languageIdHint = SptFromTermUtil.getOptional(toPart.getSubterm(0))
            .map(t -> TermUtils.asJavaString(t).orElseThrow(() -> new InvalidAstShapeException("term string", t)));
        final TestFragmentImpl fragment = TestSuiteFromTerm.fragmentFromTerm(toPart.getSubterm(2), null);
        final String resourceName = TestSuiteFromTerm.getResourceName(usedResourceNames, testSuiteDescription);
        final SptTestCaseResource resource = testCaseResourceRegistry.registerTestCase(testSuiteFile, resourceName, fragment.asText());
        return new RunToFragmentExpectation(strategyName, arguments, selection, resource.getKey(), Option.ofOptional(languageIdHint), sourceRegion);
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
