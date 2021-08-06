package mb.spt.expectation;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.jsglr.common.TermTracer;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ResolveToExpectation extends ResolveExpectation{

    public final SelectionReference toTerm;

    public ResolveToExpectation(SelectionReference fromTerm, SelectionReference toTerm, Region sourceRegion) {
        super(fromTerm, sourceRegion);
        this.toTerm = toTerm;
    }

    @Override
    protected void checkNode(IStrategoTerm node, TestCase testCase, LanguageUnderTest languageUnderTest, Session languageUnderTestSession, KeyedMessagesBuilder messagesBuilder) throws InterruptedException {
        ResourceKey file = testCase.testSuiteFile;
        Region toRegion = testCase.testFragment.getInFragmentSelections().get(toTerm.selection - 1);
        @Nullable Region nodeRegion = TermTracer.getInFragmentRegion(node);
        @Nullable ResourceKey nodeResource = TermTracer.getResourceKey(node);
        if (nodeRegion == null) {
            messagesBuilder.addMessage("Resolved to a term without region information", Severity.Error, file, sourceRegion);
        } else if(toRegion.getStartOffset() != nodeRegion.getStartOffset() || toRegion.getEndOffset() != nodeRegion.getEndOffset()) {
            messagesBuilder.addMessage(
                "Resolved to region (" + nodeRegion.getStartOffset() +
                    ", " + nodeRegion.getEndOffset() + ") instead of selection #" +
                    (toTerm.selection) + " at region (" + toRegion.getStartOffset() + ", " +
                    toRegion.getEndOffset() + ")",
                Severity.Error,
                file,
                sourceRegion
            );
        } else if (nodeResource != null && !testCase.resource.equals(nodeResource)) {
            messagesBuilder.addMessage("Resolved to a term in a different file: " + nodeResource.getIdAsString(), Severity.Error, file, sourceRegion);
        }
    }
}
