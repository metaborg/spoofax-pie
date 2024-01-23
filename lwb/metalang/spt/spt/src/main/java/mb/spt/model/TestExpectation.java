package mb.spt.model;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.hierarchical.ResourcePath;
import mb.spt.lut.LanguageUnderTestProvider;

import javax.annotation.Nullable;

public interface TestExpectation {
    KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        @Nullable ResourcePath rootDirectoryHint,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException;
}
