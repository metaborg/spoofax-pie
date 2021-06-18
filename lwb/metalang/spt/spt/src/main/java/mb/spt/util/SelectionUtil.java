package mb.spt.util;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.spt.api.model.TestCase;
import mb.spt.model.SelectionReference;

public class SelectionUtil {
    public static boolean checkSelections(TestCase testCase, Iterable<SelectionReference> selectionReferences, KeyedMessagesBuilder messagesBuilder) {
        final int numSelections = testCase.fragment.getSelections().size();
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
