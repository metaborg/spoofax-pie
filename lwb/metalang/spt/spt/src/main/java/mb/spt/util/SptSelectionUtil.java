package mb.spt.util;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;

public class SptSelectionUtil {
    public static boolean checkSelectionReferences(
        Iterable<SelectionReference> selectionReferences,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase
    ) {
        boolean noErrors = true;
        for(SelectionReference selectionReference : selectionReferences) {
            noErrors = noErrors && checkSelectionReference(selectionReference, messagesBuilder, testCase);
        }
        return noErrors;
    }

    public static boolean checkSelectionReference(
        SelectionReference selectionReference,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase
    ) {
        final int numSelections = testCase.testFragment.getSelections().size();
        if(selectionReference.selection > numSelections || selectionReference.selection <= 0) {
            messagesBuilder.addMessage("Test case does not have selection #" + selectionReference.selection, Severity.Error, testCase.testSuiteFile, selectionReference.region);
            return false;
        }
        return true;
    }
}
