package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.analyze.TestableAnalysis;
import mb.spt.api.parse.TestableParse;
import mb.spt.api.stratego.TestableStratego;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.stratego.common.StrategoException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Term;

public class ResolveToExpectation implements TestExpectation {

    public final SelectionReference fromTerm;
    public final Option<SelectionReference> toTerm;
    public final Region sourceRegion;

    public ResolveToExpectation(SelectionReference fromTerm, Option<SelectionReference> toTerm, Region sourceRegion) {
        this.fromTerm = fromTerm;
        this.toTerm = toTerm;
        this.sourceRegion = sourceRegion;
    }

    protected void checkNode(IStrategoTerm node, TestCase testCase, LanguageUnderTest languageUnderTest, Session languageUnderTestSession, KeyedMessagesBuilder messagesBuilder) throws InterruptedException {
        ResourceKey file = testCase.testSuiteFile;
        Option<Region> toRegion = toTerm.map((selectionReference) -> testCase.testFragment.getInFragmentSelections().get(selectionReference.selection - 1));
        toRegion.ifSome((refRegion) -> {
            @Nullable Region nodeRegion = TermTracer.getInFragmentRegion(node);
            @Nullable ResourceKey nodeResource = TermTracer.getResourceKey(node);
            if (nodeRegion == null) {
                messagesBuilder.addMessage("Resolved to a term without region information", Severity.Error, file, sourceRegion);
            } else if(refRegion.getStartOffset() != nodeRegion.getStartOffset() || refRegion.getEndOffset() != nodeRegion.getEndOffset()) {
                messagesBuilder.addMessage(
                    "Resolved to region (" + nodeRegion.getStartOffset() +
                        ", " + nodeRegion.getEndOffset() + ") instead of selection #" +
                        (toTerm.get().selection) + " at region (" + refRegion.getStartOffset() + ", " +
                        refRegion.getEndOffset() + ")",
                    Severity.Error,
                    file,
                    sourceRegion
                );
            } else if (nodeResource != null && !testCase.resource.equals(nodeResource)) {
                messagesBuilder.addMessage("Resolved to a term in a different file: " + nodeResource.getIdAsString(), Severity.Error, file, sourceRegion);
            }
        });
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
