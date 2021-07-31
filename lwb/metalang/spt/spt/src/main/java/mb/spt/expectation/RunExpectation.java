package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.analyze.TestableAnalysis;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class RunExpectation implements TestExpectation {
    private final String strategy;
    private final Option<SelectionReference> selectionReference;
    private final Region sourceRegion;
    private final boolean expectFailure;

    public RunExpectation(String strategy, Option<SelectionReference> selectionReference, Region sourceRegion, boolean expectFailure) {
        this.strategy = strategy;
        this.selectionReference = selectionReference;
        this.sourceRegion = sourceRegion;
        this.expectFailure = expectFailure;
    }

    protected void checkAst(
        IStrategoTerm ast,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession, LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        Region sourceRegion
    ) throws InterruptedException {}

    @Override
    public KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final ResourceKey file = testCase.testSuiteFile;
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableAnalysis)) {
            messagesBuilder.addMessage("Cannot evaluate parse to ATerm expectation because language instance '" + languageInstance + "' does not implement TestableAnalysis", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableAnalysis testableAnalysis = (TestableAnalysis)languageInstance;
        Option<Region> region = selectionReference.map(
            (sel) -> testCase.testFragment.getSelections().get(sel.selection - 1)
        );
        final Result<IStrategoTerm, ?> result = testableAnalysis.testRunStrategy(languageUnderTestSession, testCase.resource, strategy, region, testCase.rootDirectoryHint);

        result
            .ifOkThrowing(
                (o) -> {
                    if (expectFailure) {
                        messagesBuilder.addMessage("Expected strategy to fail, but it succeeded", Severity.Error, file, sourceRegion);
                    }
                    messagesBuilder.addMessage(o.toString(), Severity.Info, file, sourceRegion);
                    checkAst(o, messagesBuilder, testCase, languageUnderTest, languageUnderTestSession, languageUnderTestProvider, context, sourceRegion);
                }
            )
            .ifErr(
                (e) -> {
                    if (!expectFailure) {
                        messagesBuilder.addMessage(
                            "Expected executing strategy '" + strategy + "' to succeed but it threw an exception",
                            e,
                            Severity.Error,
                            file,
                            sourceRegion
                        );
                    }
                }
            );

        return messagesBuilder.build();
    }
}
