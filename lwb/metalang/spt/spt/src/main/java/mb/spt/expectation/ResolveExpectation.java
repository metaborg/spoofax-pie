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
import mb.spt.api.stratego.TestableStratego;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.stratego.common.StrategoException;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ResolveExpectation implements TestExpectation {

    public final SelectionReference fromTerm;
    public final Region sourceRegion;

    public ResolveExpectation(SelectionReference fromTerm, Region sourceRegion) {
        this.fromTerm = fromTerm;
        this.sourceRegion = sourceRegion;
    }

    protected void checkNode(IStrategoTerm node, TestCase testCase, LanguageUnderTest languageUnderTest, Session languageUnderTestSession, KeyedMessagesBuilder messagesBuilder) throws InterruptedException {

    }

    @Override
    public KeyedMessages evaluate(TestCase testCase, LanguageUnderTest languageUnderTest, Session languageUnderTestSession, LanguageUnderTestProvider languageUnderTestProvider, ExecContext context, CancelToken cancel) throws InterruptedException {
        KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        ResourceKey file = testCase.testSuiteFile;
        Region fromRegion = testCase.testFragment.getInFragmentSelections().get(fromTerm.selection - 1);
        LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();

        if (!(languageInstance instanceof TestableStratego)) {
            messagesBuilder.addMessage("Cannot evaluate run expectation because language instance '" + languageInstance + "' does not implement TestableStratego", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }

        final Result<IStrategoTerm, ?> result = ((TestableStratego)languageInstance).testResolve(languageUnderTestSession, testCase.resource, fromRegion, testCase.rootDirectoryHint);

        result
            .ifOkThrowing((node) -> {
                checkNode(node, testCase, languageUnderTest, languageUnderTestSession, messagesBuilder);
            })
            .ifErr((e) -> {
                if (e instanceof StrategoException) {
                    messagesBuilder.addMessage(
                        ((StrategoException)e).getMessage(),
                        e,
                        Severity.Error,
                        file,
                        sourceRegion
                    );
                } else {
                    messagesBuilder.extractMessagesRecursively(e);
                }
            });

        return messagesBuilder.build(testCase.testSuiteFile);
    }
}
