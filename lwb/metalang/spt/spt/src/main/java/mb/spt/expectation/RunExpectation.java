package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.jsglr.common.TermTracer;
import mb.jsglr.pie.JsglrParseTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.analyze.TestableAnalysis;
import mb.spt.api.parse.TestableParse;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import org.checkerframework.checker.units.qual.K;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class RunExpectation implements TestExpectation {
    private final String strategy;
    private final Option<SelectionReference> selectionReference;
    private final Region sourceRegion;

    public RunExpectation(String strategy, Option<SelectionReference> selectionReference, Region sourceRegion) {
        this.strategy = strategy;
        this.selectionReference = selectionReference;
        this.sourceRegion = sourceRegion;
    }

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
            .ifOk(
                (o) -> messagesBuilder.addMessage(o.toString(), Severity.Warning, file, sourceRegion)
            )
            .ifErr(
            (e) -> messagesBuilder.addMessage(
                    "Expected executing strategy '" + strategy + "' to succeed but it threw an exception",
                    e,
                    Severity.Error,
                    file,
                    sourceRegion
            )
        );

        return messagesBuilder.build();
    }
}
