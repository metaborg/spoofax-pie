package mb.spt.fromterm;

import mb.common.region.Region;
import mb.common.text.FragmentedString;
import mb.common.text.StringFragment;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.CoordinateRequirement;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.model.TestFragment;
import mb.spt.model.TestSuite;
import mb.spt.model.TestFixture;
import mb.spt.model.TestFragmentImpl;
import mb.spt.resource.SptTestCaseResource;
import mb.spt.resource.SptTestCaseResourceRegistry;
import mb.spt.util.SptFromTermUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class TestSuiteFromTerm {
    public static TestSuite testSuiteFromTerm(
        IStrategoTerm ast,
        MapView<IStrategoConstructor, TestExpectationFromTerm> expectationsFromTerms,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint
    ) throws FromTermException {
        testCaseResourceRegistry.clearTestSuite(file);

        final IStrategoAppl appl = TermUtils.asAppl(ast).orElseThrow(() -> new InvalidAstShapeException("a term application", ast));
        if(!TermUtils.isAppl(appl, "TestSuite", 3)) { // TestSuite(<Header+> <TestFixture?> <TestDecl*>)
            throw new InvalidAstShapeException("a TestSuite/3 term application", appl);
        }

        String name = "";
        @Nullable CoordinateRequirement languageCoordinateRequirementHint = null;
        final IStrategoList headers = TermUtils.asListAt(appl, 0).orElseThrow(() -> new InvalidAstShapeException("a term list as first subterm", appl));
        for(IStrategoTerm header : headers) {
            if(TermUtils.isAppl(header, "Name", 1)) {
                name = TermUtils.asJavaStringAt(header, 0).orElseThrow(() -> new InvalidAstShapeException("a string as first subterm", header));
            } else if(TermUtils.isAppl(header, "Language", 1)) {
                final String requirement = TermUtils.asJavaStringAt(header, 0).orElseThrow(() -> new InvalidAstShapeException("a string as first subterm", header));
                languageCoordinateRequirementHint = CoordinateRequirement.parse(requirement).get();
            }
        }
        if(name.isEmpty()) {
            // TODO: add error?
        }


        final @Nullable TestFixture testFixture = SptFromTermUtil.getOptional(appl.getSubterm(1))
            .map(testFixtureTerm -> { // Fixture("[[", <text1>, "[[", "]]", <text2>, "]]")
                if(!TermUtils.isAppl(testFixtureTerm, "Fixture", 6)) {
                    throw new InvalidAstShapeException("a Fixture/6 term application", testFixtureTerm);
                }
                final Region beforeRegion = TermTracer.getRegionOptional(testFixtureTerm.getSubterm(1))
                    .orElseThrow(() -> new InvalidAstShapeException("test fixture start text term as first subterm with location information", testFixtureTerm));
                final String beforeText = TermUtils.asJavaStringAt(testFixtureTerm, 1)
                    .orElseThrow(() -> new InvalidAstShapeException("test fixture start text as first subterm", testFixtureTerm));
                final Region afterRegion = TermTracer.getRegionOptional(testFixtureTerm.getSubterm(4))
                    .orElseThrow(() -> new InvalidAstShapeException("test fixture start text term as fifth subterm with location information", testFixtureTerm));
                final String afterText = TermUtils.asJavaStringAt(testFixtureTerm, 4)
                    .orElseThrow(() -> new InvalidAstShapeException("test fixture start text as fifth subterm", testFixtureTerm));
                return new TestFixture(beforeText, beforeRegion.getStartOffset(), afterText, afterRegion.getStartOffset());
            })
            .get();

        final IStrategoList testCaseTerms = TermUtils.asListAt(appl, 2).orElseThrow(() -> new InvalidAstShapeException("a term list as third subterm", appl));
        final ArrayList<TestCase> testCases = new ArrayList<>(testCaseTerms.size());
        final HashSet<String> usedResourceNames = new HashSet<>();
        for(IStrategoTerm testCaseTerm : testCaseTerms) {
            final TestCase testCase = testCaseFromTerm(testCaseTerm, testFixture, file, rootDirectoryHint, usedResourceNames, expectationsFromTerms, testCaseResourceRegistry);
            testCases.add(testCase);
        }

        return new TestSuite(name, file, ListView.of(testCases), rootDirectoryHint, languageCoordinateRequirementHint);
    }

    private static TestCase testCaseFromTerm(
        IStrategoTerm ast,
        @Nullable TestFixture testFixture,
        ResourceKey suiteFile,
        @Nullable ResourcePath rootDirectoryHint,
        HashSet<String> usedResourceNames,
        MapView<IStrategoConstructor, TestExpectationFromTerm> expectationsFromTerms,
        SptTestCaseResourceRegistry testCaseResourceRegistry
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
        final TestFragment testFragment = fragmentFromTerm(fragmentTerm, testFixture);
        final IStrategoList testExpectationTerms = TermUtils.asListAt(appl, 4).orElseThrow(() -> new InvalidAstShapeException("a term list as fifth subterm", appl));
        final ArrayList<TestExpectation> testExpectations = new ArrayList<>(testExpectationTerms.size());
        for(IStrategoTerm testExpectationTerm : testExpectationTerms) {
            testExpectations.add(testExpectationFromTerm(testExpectationTerm, expectationsFromTerms, descriptionRegion, description, suiteFile, usedResourceNames, testCaseResourceRegistry));
        }
        final String resourceName = getResourceName(usedResourceNames, description);
        final SptTestCaseResource resource = testCaseResourceRegistry.registerTestCase(suiteFile, resourceName, testFragment.asText());
        return new TestCase(resource.getPath(), suiteFile, rootDirectoryHint, description, descriptionRegion, testFragment, ListView.of(testExpectations));
    }

    public static String getResourceName(HashSet<String> usedResourceNames, String name) {
        name = name.trim();
        int i = 1;
        while(usedResourceNames.contains(name)) {
            if(i > 1) name = name.substring(0, name.length() - 1) + i;
            else name = name + i;
            ++i;
        }
        usedResourceNames.add(name);
        return name;
    }

    public static TestFragmentImpl fragmentFromTerm(IStrategoTerm ast, @Nullable TestFixture testFixture) throws FromTermException {
        final @Nullable Region region = TermTracer.getRegion(ast);
        if(region == null) {
            throw new InvalidAstShapeException("a fragment term with location information", ast);
        }
        final ArrayList<Region> selections = new ArrayList<>();
        final ArrayList<Region> inFragmentSelections = new ArrayList<>();
        final ArrayList<StringFragment> stringFragments = new ArrayList<>();
        int offset = 0;
        if(testFixture != null) {
            stringFragments.add(new StringFragment(testFixture.beforeStartOffset, testFixture.beforeText));
            offset += testFixture.beforeText.length();
        }
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
                    stringFragments.add(new StringFragment(textRegion.getStartOffset(), textTerm.stringValue()));
                    // Tail
                    appl = TermUtils.asApplAt(appl, 1).orElseThrow(() -> new InvalidAstShapeException("a tail part as second subterm", fragment));
                    offset += textRegion.getLength();
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
                    stringFragments.add(new StringFragment(selectionTextRegion.getStartOffset(), selectionTextTerm.stringValue()));
                    selections.add(selectionTextRegion);
                    inFragmentSelections.add(Region.fromOffsetLength(offset, selectionTextRegion.getLength()));
                    // Text
                    final IStrategoString textTerm = TermUtils.asStringAt(more, 1).orElseThrow(() -> new InvalidAstShapeException("a term string as second subterm", more));
                    final @Nullable Region textRegion = TermTracer.getRegion(textTerm);
                    if(textRegion == null) {
                        throw new InvalidAstShapeException("a more text term with location information", textTerm);
                    }
                    stringFragments.add(new StringFragment(textRegion.getStartOffset(), textTerm.stringValue()));
                    // Tail
                    appl = TermUtils.asApplAt(appl, 2).orElseThrow(() -> new InvalidAstShapeException("a tail part as third subterm", more));
                    offset += selectionTextRegion.getLength() + textRegion.getLength();
                    break;
                }
            }
        }
        if(testFixture != null) {
            stringFragments.add(new StringFragment(testFixture.afterStartOffset, testFixture.afterText));
        }
        return new TestFragmentImpl(region, ListView.of(selections), ListView.of(inFragmentSelections), new FragmentedString(ListView.of(stringFragments)));
    }

    private static TestExpectation testExpectationFromTerm(
        IStrategoTerm ast,
        MapView<IStrategoConstructor, TestExpectationFromTerm> expectationsFromTerms,
        Region fallbackRegion,
        String testSuiteDescription,
        ResourceKey testSuiteFile,
        HashSet<String> usedResourceNames,
        SptTestCaseResourceRegistry testCaseResourceRegistry
    ) throws FromTermException {
        final IStrategoAppl appl = TermUtils.asAppl(ast).orElseThrow(() -> new InvalidAstShapeException("a term application", ast));
        final @Nullable TestExpectationFromTerm testExpectationFromTerm = expectationsFromTerms.get(appl.getConstructor());
        if(testExpectationFromTerm == null) {
            throw new InvalidAstShapeException("a supported TestExpectationFromTerm instance for constructor of this term application", appl);
        }
        return testExpectationFromTerm.convert(appl, fallbackRegion, testSuiteDescription, testSuiteFile, testCaseResourceRegistry, usedResourceNames);
    }
}
