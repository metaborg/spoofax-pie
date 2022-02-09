package mb.spt.expectation;

import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.util.SetView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.spt.expectation.CheckCountExpectation.Operator;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.model.TestExpectation;
import mb.spt.resource.SptTestCaseResourceRegistry;
import mb.spt.util.SptFromTermUtil;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.model.SelectionReference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class CheckExpectationsFromTerm implements TestExpectationFromTerm {
    @Override public SetView<IStrategoConstructor> getMatchingConstructors(TermFactory termFactory) {
        return SetView.of(
            termFactory.makeConstructor("AnalyzeMessages", 4),
            termFactory.makeConstructor("AnalyzeMessagePattern", 3)
        );
    }

    @Override public TestExpectation convert(
        IStrategoAppl term,
        Region fallbackRegion,
        String testSuiteDescription, ResourceKey testSuiteFile, SptTestCaseResourceRegistry testCaseResourceRegistry,
        HashSet<String> usedResourceNames
    ) throws FromTermException {
        final @Nullable Region termSourceRegion = TermTracer.getRegion(term);
        final Region sourceRegion = termSourceRegion != null ? termSourceRegion : fallbackRegion;
        final IStrategoConstructor constructor = term.getConstructor();
        switch(constructor.getName()) {
            case "AnalyzeMessages":
                return convertCheckCount(term, sourceRegion);
            case "AnalyzeMessagePattern":
                return convertCheckPattern(term, sourceRegion);
            default:
                throw new FromTermException("Cannot convert term '" + term + "' to a check expectation; term is not a valid check expectation (no matching constructor)");
        }
    }

    private CheckCountExpectation convertCheckCount(IStrategoAppl term, Region sourceRegion) { // <<MessageOp?> <INT> <Severity> <AtPart?>>
        final Operator operator = convertOperator(TermUtils.asApplAt(term, 0)
            .orElseThrow(() -> new InvalidAstShapeException("an optional operator term as first subterm", term)));
        final int count = TermUtils.asJavaIntAt(term, 1)
            .orElseThrow(() -> new InvalidAstShapeException("a count as second subterm", term));
        final Severity severity = convertSeverity(TermUtils.asApplAt(term, 2)
            .orElseThrow(() -> new InvalidAstShapeException("a severity term as third subterm", term)));
        final ArrayList<SelectionReference> selections = convertSelections(term.getSubterm(3), sourceRegion);
        return new CheckCountExpectation(operator, count, severity, selections, sourceRegion);
    }

    private TestExpectation convertCheckPattern(IStrategoAppl term, Region sourceRegion) { // <<Severity> like <STRING> <AtPart?>>
        final Severity severity = convertSeverity(TermUtils.asApplAt(term, 0)
            .orElseThrow(() -> new InvalidAstShapeException("a severity term as first subterm", term)));
        final String like = TermUtils.asJavaStringAt(term, 1)
            .orElseThrow(() -> new InvalidAstShapeException("a like string term as second subterm", term));
        final ArrayList<SelectionReference> selections = convertSelections(term.getSubterm(2), sourceRegion);
        return new CheckPatternExpectation(severity, like, selections, sourceRegion);
    }


    private Operator convertOperator(IStrategoTerm term) {
        return SptFromTermUtil.getOptional(term)
            .map(someTerm -> {
                final IStrategoAppl operatorAppl = TermUtils.asAppl(someTerm)
                    .orElseThrow(() -> new InvalidAstShapeException("a term application", someTerm));
                switch(operatorAppl.getConstructor().getName()) {
                    case "Equal":
                        return Operator.Equal;
                    case "Less":
                        return Operator.Less;
                    case "LessOrEqual":
                        return Operator.LessOrEqual;
                    case "More":
                        return Operator.More;
                    case "MoreOrEqual":
                        return Operator.MoreOrEqual;
                    default:
                        throw new FromTermException("Cannot convert term '" + someTerm + "' to an Operator; term is not a valid operator (no matching constructor)");
                }
            })
            .unwrapOr(Operator.Equal);
    }


    private Severity convertSeverity(IStrategoTerm term) {
        final IStrategoAppl appl = TermUtils.asAppl(term)
            .orElseThrow(() -> new InvalidAstShapeException("a term application", term));
        switch(appl.getConstructor().getName()) {
            case "Error":
                return Severity.Error;
            case "Warning":
                return Severity.Warning;
            case "Note":
                return Severity.Info;
            default:
                throw new FromTermException("Cannot convert term '" + appl + "' to a Severity; term is not a valid severity (no matching constructor)");
        }
    }

    private ArrayList<SelectionReference> convertSelections(IStrategoTerm term, Region fallbackRegion) {
        return SptFromTermUtil.getOptional(term)
            .map(someTerm -> {
                final IStrategoList selectionsList = TermUtils.asListAt(someTerm, 0)
                    .orElseThrow(() -> new InvalidAstShapeException("a term list as first subterm", someTerm));
                final ArrayList<SelectionReference> selections = new ArrayList<>(selectionsList.size());
                for(IStrategoTerm selectionTerm : selectionsList) {
                    final int selection = TermUtils.asJavaInt(selectionTerm).orElseThrow(() -> new InvalidAstShapeException("an integer", selectionTerm));
                    final @Nullable Region region = TermTracer.getRegion(selectionTerm);
                    selections.add(new SelectionReference(selection, region != null ? region : fallbackRegion));
                }
                return selections;
            })
            .unwrapOr(new ArrayList<>(0));
    }
}
