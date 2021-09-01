package mb.spt.expectation;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.util.SptSelectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckExpectationUtil {
    public static boolean checkSelections(
        Stream<Message> messages,
        Iterable<SelectionReference> selectionReferences,
        CheckCountExpectation.Operator operator,
        long expectedCount,
        String expected,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase
    ) {
        if(!SptSelectionUtil.checkSelectionReferences(selectionReferences, messagesBuilder, testCase)) {
            return false;
        }
        boolean success = true;
        final ArrayList<Region> messageRegions = messages
            .filter(m -> m.region != null)
            .map(m -> m.region)
            .collect(Collectors.toCollection(ArrayList::new));
        for(SelectionReference selectionReference : selectionReferences) {
            final Region selection = testCase.testFragment.getSelections().get(Math.max(0, selectionReference.selection - 1));
            final Iterator<Region> iterator = messageRegions.iterator();
            int count = 0;
            while(iterator.hasNext()) {
                final Region messageRegion = iterator.next();
                if(selection.contains(messageRegion)) {
                    iterator.remove();
                    ++count;
                    if(expectedCount == -1 || operator.test(count, expectedCount)) {
                        break;
                    }
                }
            }
            if(count == 0 || (expectedCount != -1 && !operator.test(count, expectedCount))) {
                messagesBuilder.addMessage("Expected " + expected + " at selection " + selectionReference.selection, Severity.Error, testCase.testSuiteFile, selectionReference.region);
                success = false;
            }
        }
        return success;
    }
}
