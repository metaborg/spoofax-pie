package mb.spt.expectation;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.spt.api.model.TestCase;
import mb.spt.model.SelectionReference;
import mb.spt.util.SelectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckExpectationUtil {
    public static boolean checkSelections(
        Stream<Message> messages,
        Iterable<SelectionReference> selectionReferences,
        String expected,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase
    ) {
        if(!SelectionUtil.checkSelectionReferences(selectionReferences, messagesBuilder, testCase)) {
            return false;
        }
        final ArrayList<Region> messageRegions = messages
            .filter(m -> m.region != null)
            .map(m -> m.region)
            .collect(Collectors.toCollection(ArrayList::new));
        for(SelectionReference selectionReference : selectionReferences) {
            final Region selection = testCase.testFragment.getSelections().get(selectionReference.selection - 1);
            final Iterator<Region> iterator = messageRegions.iterator();
            boolean found = false;
            while(iterator.hasNext()) {
                final Region messageRegion = iterator.next();
                if(selection.contains(messageRegion)) {
                    iterator.remove();
                    found = true;
                    break;
                }
            }
            if(!found) {
                messagesBuilder.addMessage("Expected " + expected + " at selection " + selectionReference.selection + ", but found none", Severity.Error, testCase.testSuiteFile, selectionReference.region);
                return false;
            }
        }
        return true;
    }
}
