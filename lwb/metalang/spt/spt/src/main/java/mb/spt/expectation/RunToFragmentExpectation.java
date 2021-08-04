package mb.spt.expectation;

import mb.aterm.common.TermToString;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.analyze.TestableAnalysis;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.strategoxt.lang.TermEqualityUtil;

public class RunToFragmentExpectation extends RunExpectation {
    private final ResourceKey fragmentResource;
    private final Option<String> languageIdHint;

    public RunToFragmentExpectation(
        String strategyName,
        ListView<IStrategoAppl> arguments,
        Option<SelectionReference> selection,
        ResourceKey fragmentResource,
        Option<String> languageIdHint,
        Region sourceRegion
    ) {
        super(strategyName, arguments, selection, sourceRegion, false);
        this.fragmentResource = fragmentResource;
        this.languageIdHint = languageIdHint;
    }

    @Override
    protected void checkAst(
        IStrategoTerm ast,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession, LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        Region sourceRegion
    ) throws InterruptedException {
        ResourceKey file = testCase.testSuiteFile;
        final @Nullable LanguageUnderTest fragmentLanguageUnderTest = ExpectationFragmentUtil.getLanguageUnderTest(testCase, languageUnderTest, languageUnderTestProvider, context, languageIdHint.get());
        if (fragmentLanguageUnderTest == null) {
            messagesBuilder.addMessage(
                "Cannot evaluate run to fragment expectation because providing language under test for language id '" + languageIdHint + "' failed unexpectedly",
                Severity.Error,
                file,
                sourceRegion
            );
            return;
        }
        final LanguageInstance fragmentInstance = fragmentLanguageUnderTest.getLanguageComponent().getLanguageInstance();
        if (!(fragmentInstance instanceof TestableAnalysis)) {
            messagesBuilder.addMessage(
                "Cannot evaluate run to fragment expectation because language instance '" + fragmentInstance + "' does not implement TestableAnalysis",
                Severity.Error,
                file,
                sourceRegion
            );
            return;
        }
        final TestableAnalysis testableParse = (TestableAnalysis)fragmentInstance;
        final Session session;
        if (fragmentLanguageUnderTest == languageUnderTest) {
            session = languageUnderTestSession;
        } else {
            session = fragmentLanguageUnderTest.getPieComponent().newSession();
        }
        final Result<IStrategoTerm, ?> result = testableParse.testAnalyze(session, fragmentResource, testCase.rootDirectoryHint);

        result
            .ifElse((expectedAst) -> {
                if (!TermEqualityUtil.equalsIgnoreAnnos(ast, expectedAst, new TermFactory())) {
                    messagesBuilder.addMessage(
                        "Expected run to " + TermToString.toString(expectedAst) + ", but got " + TermToString.toString(ast),
                        Severity.Error,
                        file,
                        sourceRegion
                    );
                }
            }, e -> messagesBuilder.addMessage("Failed to analyze reference fragment; see exception", e, Severity.Error, file, sourceRegion)
            );
    }
}
