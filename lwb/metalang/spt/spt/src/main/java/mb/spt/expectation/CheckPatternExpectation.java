package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.spt.api.model.LanguageUnderTest;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;
import mb.spt.model.SelectionReference;

import java.util.ArrayList;

public class CheckPatternExpectation implements TestExpectation {
    private final Severity severity;
    private final String like;
    private final ArrayList<SelectionReference> selections;
    private final Region sourceRegion;

    public CheckPatternExpectation(Severity severity, String like, ArrayList<SelectionReference> selections, Region sourceRegion) {
        this.severity = severity;
        this.like = like;
        this.selections = selections;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, CancelToken cancel, TestCase testCase) throws InterruptedException {
        return KeyedMessages.of(); // TODO
    }
}
