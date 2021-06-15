package mb.spt.fromterm;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spt.api.model.Fragment;
import mb.spt.model.FragmentImpl;
import mb.spt.api.model.FragmentPart;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;
import mb.spt.api.model.TestSuite;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;

public class TestSuiteFromTerm {
    public static TestSuite testSuiteFromTerm(
        IStrategoTerm ast,
        MapView<IStrategoConstructor, TestExpectationFromTerm> expectationsFromTerms,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint
    ) throws FromTermException {
        final IStrategoAppl appl = TermUtils.asAppl(ast).orElseThrow(() -> new InvalidAstShapeException("a term application", ast));
        if(!TermUtils.isAppl(appl, "TestSuite", 3)) {
            throw new InvalidAstShapeException("a TestSuite/3 term application", appl);
        }

        String name = "";
        @Nullable String languageIdHint = null;
        final IStrategoList headers = TermUtils.asListAt(appl, 0).orElseThrow(() -> new InvalidAstShapeException("a term list as first subterm", appl));
        for(IStrategoTerm header : headers) {
            if(TermUtils.isAppl(header, "Name", 1)) {
                name = TermUtils.asJavaStringAt(header, 0).orElseThrow(() -> new InvalidAstShapeException("a string as first subterm", header));
            } else if(TermUtils.isAppl(header, "Language", 1)) {
                languageIdHint = TermUtils.asJavaStringAt(header, 0).orElseThrow(() -> new InvalidAstShapeException("a string as first subterm", header));
            }
        }
        if(name.isEmpty()) {
            // TODO: add error
        }

        // TODO: test fixture

        final IStrategoList testCaseTerms = TermUtils.asListAt(appl, 2).orElseThrow(() -> new InvalidAstShapeException("a term list as third subterm", appl));
        final ArrayList<TestCase> testCases = new ArrayList<>(testCaseTerms.size());
        for(IStrategoTerm testCaseTerm : testCaseTerms) {
            final TestCase testCase = testCaseFromTerm(testCaseTerm, file, rootDirectoryHint, expectationsFromTerms);
            testCases.add(testCase);
        }

        return new TestSuite(name, file, ListView.of(testCases), languageIdHint, rootDirectoryHint);
    }

    public static TestCase testCaseFromTerm(
        IStrategoTerm ast,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        MapView<IStrategoConstructor, TestExpectationFromTerm> expectationsFromTerms
    ) throws FromTermException {
        final IStrategoAppl appl = TermUtils.asAppl(ast).orElseThrow(() -> new InvalidAstShapeException("a term application", ast));
        if(!TermUtils.isAppl(appl, "Test", 5)) {
            throw new InvalidAstShapeException("a Test/5 term application", appl);
        }
        final IStrategoString descriptionTerm = TermUtils.asStringAt(appl, 0).orElseThrow(() -> new InvalidAstShapeException("a term string as first subterm", appl));
        final String description = descriptionTerm.stringValue();
        final @Nullable Region descriptionRegion = TermTracer.getRegion(descriptionTerm);
        if(descriptionRegion == null) {
            throw new InvalidAstShapeException("a description term with location information", descriptionTerm);
        }
        final IStrategoAppl fragmentTerm = TermUtils.asApplAt(ast, 2).orElseThrow(() -> new InvalidAstShapeException("a term application as third subterm", ast));
        final Fragment fragment = fragmentFromTerm(fragmentTerm);
        final IStrategoList testExpectationTerms = TermUtils.asListAt(appl, 4).orElseThrow(() -> new InvalidAstShapeException("a term list as fifth subterm", appl));
        final ArrayList<TestExpectation> testExpectations = new ArrayList<>(testExpectationTerms.size());
        for(IStrategoTerm testExpectationTerm : testExpectationTerms) {
            testExpectations.add(testExpectationFromTerm(testExpectationTerm, expectationsFromTerms));
        }
        return new TestCase(file, rootDirectoryHint, description, descriptionRegion, fragment, ListView.of(testExpectations));
    }

    public static FragmentImpl fragmentFromTerm(IStrategoTerm ast) throws FromTermException {
        final @Nullable Region region = TermTracer.getRegion(ast);
        if(region == null) {
            throw new InvalidAstShapeException("a fragment term with location information", ast);
        }
        final ArrayList<Region> selections = new ArrayList<>();
        final ArrayList<FragmentPart> parts = new ArrayList<>();
        IStrategoAppl appl = TermUtils.asAppl(ast).orElseThrow(() -> new InvalidAstShapeException("a term application", ast));
        while(!appl.getConstructor().getName().equals("Done")) {
            switch(appl.getConstructor().getName()) {
                case "Fragment": { // Fragment("text", <TailPart>)
                    final IStrategoAppl fragment = appl;
                    // Text
                    final IStrategoString textTerm = TermUtils.asStringAt(fragment, 0).orElseThrow(() -> new InvalidAstShapeException("a term string as first subterm", fragment));
                    final @Nullable Region textRegion = TermTracer.getRegion(textTerm);
                    if(textRegion == null) {
                        throw new InvalidAstShapeException("a fragment text term with location information", textTerm);
                    }
                    parts.add(new FragmentPart(textRegion.getStartOffset(), textTerm.stringValue()));
                    // Tail
                    appl = TermUtils.asApplAt(appl, 1).orElseThrow(() -> new InvalidAstShapeException("a tail part as second subterm", fragment));
                    break;
                }
                case "More": { // More(Selection("[", "text", "]"), "text", <TailPart>)
                    final IStrategoAppl more = appl;
                    // Selection
                    final IStrategoAppl selection = TermUtils.asApplAt(more, 0).orElseThrow(() -> new InvalidAstShapeException("a term application as first subterm", more));
                    if(!TermUtils.isAppl(selection, "Selection", 3)) {
                        throw new InvalidAstShapeException("a Selection/3 term application", selection);
                    }
                    final IStrategoString selectionTextTerm = TermUtils.asStringAt(selection, 1).orElseThrow(() -> new InvalidAstShapeException("a term string as second subterm", selection));
                    final @Nullable Region selectionTextRegion = TermTracer.getRegion(selectionTextTerm);
                    if(selectionTextRegion == null) {
                        throw new InvalidAstShapeException("a selection term with location information", selectionTextTerm);
                    }
                    parts.add(new FragmentPart(selectionTextRegion.getStartOffset(), selectionTextTerm.stringValue()));
                    selections.add(selectionTextRegion);
                    // Text
                    final IStrategoString textTerm = TermUtils.asStringAt(more, 1).orElseThrow(() -> new InvalidAstShapeException("a term string as second subterm", more));
                    final @Nullable Region textRegion = TermTracer.getRegion(textTerm);
                    if(textRegion == null) {
                        throw new InvalidAstShapeException("a more text term with location information", textTerm);
                    }
                    parts.add(new FragmentPart(textRegion.getStartOffset(), textTerm.stringValue()));
                    // Tail
                    appl = TermUtils.asApplAt(appl, 2).orElseThrow(() -> new InvalidAstShapeException("a tail part as third subterm", more));
                    break;
                }
            }
        }
        return new FragmentImpl(region, ListView.of(selections), ListView.of(parts));
    }

    public static TestExpectation testExpectationFromTerm(
        IStrategoTerm ast,
        MapView<IStrategoConstructor, TestExpectationFromTerm> expectationsFromTerms
    ) throws FromTermException {
        final IStrategoAppl appl = TermUtils.asAppl(ast).orElseThrow(() -> new InvalidAstShapeException("a term application", ast));
        final @Nullable TestExpectationFromTerm testExpectationFromTerm = expectationsFromTerms.get(appl.getConstructor());
        if(testExpectationFromTerm == null) {
            throw new InvalidAstShapeException("a supported TestExpectationFromTerm instance for constructor of this term application", appl);
        }
        return testExpectationFromTerm.convert(appl);
    }
}
