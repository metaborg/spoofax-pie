package mb.spt.expectation;

import mb.common.editor.ReferenceResolutionResult;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.resolve.TestableResolve;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.util.SptSelectionUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ResolveExpectation implements TestExpectation {
    public final SelectionReference fromTerm;
    public final Region sourceRegion;

    public ResolveExpectation(SelectionReference fromTerm, Region sourceRegion) {
        this.fromTerm = fromTerm;
        this.sourceRegion = sourceRegion;
    }

    protected void checkResults(
        ReferenceResolutionResult result,
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        KeyedMessagesBuilder messagesBuilder
    ) throws InterruptedException {

    }

    @Override
    public KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        @Nullable ResourcePath rootDirectoryHint,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.testSuiteFile;

        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableResolve)) {
            messagesBuilder.addMessage("Cannot evaluate resolve expectation because language instance '" + languageInstance + "' does not implement TestableResolve", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }

        if(!SptSelectionUtil.checkSelectionReference(fromTerm, messagesBuilder, testCase)) {
            return messagesBuilder.build(file);
        }
        final Region fromRegion = testCase.testFragment.getInFragmentSelections().get(fromTerm.selection - 1);

        final Result<ReferenceResolutionResult, ?> result = ((TestableResolve)languageInstance).testResolve(languageUnderTestSession, testCase.resource, fromRegion, testCase.rootDirectoryHint);
        result
            .ifOkThrowing((resolution) -> checkResults(resolution, testCase, languageUnderTest, languageUnderTestSession, messagesBuilder))
            .ifErr((e) -> messagesBuilder.addMessage(
                e.getMessage(),
                e,
                Severity.Error,
                file,
                sourceRegion
            ));
        return messagesBuilder.build(file);
    }
}
