package mb.spt.util;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.spt.model.TestCase;
import mb.spt.model.SelectionReference;

public class SptSelectionUtil {
    public static boolean checkSelectionReferences(Iterable<SelectionReference> selectionReferences, KeyedMessagesBuilder messagesBuilder, TestCase testCase) {
        final int numSelections = testCase.testFragment.getSelections().size();
        boolean errors = false;
        for(SelectionReference selectionReference : selectionReferences) {
            if(selectionReference.selection > numSelections) {
                messagesBuilder.addMessage("Test case does not have selection #" + selectionReference.selection, Severity.Error, testCase.testSuiteFile, selectionReference.region);
                errors = true;
            }
        }
        return !errors;
    }
}
