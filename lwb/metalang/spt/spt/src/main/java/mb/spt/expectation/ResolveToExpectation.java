package mb.spt.expectation;

import mb.common.editor.ReferenceResolutionResult;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.util.SptSelectionUtil;

public class ResolveToExpectation extends ResolveExpectation {
    public final SelectionReference toTerm;

    public ResolveToExpectation(SelectionReference fromTerm, SelectionReference toTerm, Region sourceRegion) {
        super(fromTerm, sourceRegion);
        this.toTerm = toTerm;
    }

    @Override
    protected void checkResults(ReferenceResolutionResult result, TestCase testCase, LanguageUnderTest languageUnderTest, Session languageUnderTestSession, KeyedMessagesBuilder messagesBuilder) throws InterruptedException {
        final ResourceKey file = testCase.testSuiteFile;

        if(!SptSelectionUtil.checkSelectionReference(toTerm, messagesBuilder, testCase)) {
            return;
        }
        final Region toRegion = testCase.testFragment.getInFragmentSelections().get(toTerm.selection - 1);

        final ListView<ReferenceResolutionResult.ResolvedEntry> entries = result.getEntries();
        if(entries.isEmpty()) {
            messagesBuilder.addMessage("Term does not resolve to any other term", Severity.Error, file, sourceRegion);
        } else {
            boolean found = false;
            for(ReferenceResolutionResult.ResolvedEntry entry : entries) {
                Region nodeRegion = entry.getRegion();
                ResourceKey nodeResource = entry.getFile();
                if(toRegion.getStartOffset() == nodeRegion.getStartOffset() && toRegion.getEndOffset() == nodeRegion.getEndOffset()) {
                    if(testCase.resource.equals(nodeResource)) {
                        found = true;
                        break;
                    }
                }
            }
            if(!found) {
                ReferenceResolutionResult.ResolvedEntry entry = entries.get(0);
                Region nodeRegion = entry.getRegion();
                ResourceKey nodeResource = entry.getFile();
                messagesBuilder.addMessage(
                    "Resolved to region (" + nodeRegion.getStartOffset() +
                        ", " + nodeRegion.getEndOffset() + ") in file " + nodeResource + " instead of selection #" +
                        (toTerm.selection) + " at region (" + toRegion.getStartOffset() + ", " +
                        toRegion.getEndOffset() + ")",
                    Severity.Error,
                    file,
                    sourceRegion
                );
            }
        }
    }
}
