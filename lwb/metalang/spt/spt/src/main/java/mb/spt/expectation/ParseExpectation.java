package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.model.LanguageUnderTest;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;
import mb.spt.api.parse.ExpectedParseResult;
import mb.spt.api.parse.ParseResult;
import mb.spt.api.parse.TestableParse;
import mb.spt.util.MessageRemap;

public class ParseExpectation implements TestExpectation {
    private final ExpectedParseResult expected;
    private final Region sourceRegion;

    public ParseExpectation(ExpectedParseResult expected, Region sourceRegion) {
        this.expected = expected;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, CancelToken cancel, TestCase testCase) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.testSuiteFile;
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableParse)) {
            messagesBuilder.addMessage("Cannot evaluate parse expectation because language instance '" + languageInstance + "' does not implement TestableParse", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableParse testableParse = (TestableParse)languageInstance;
        final Result<ParseResult, ?> result = testableParse.testParse(session, testCase);
        result.ifElse(r -> {
            final boolean actualSuccess = r.success && !r.messages.containsError();
            final boolean[] addParseMessages = {false};
            if(expected.success != actualSuccess) {
                addParseMessages[0] = true;
                messagesBuilder.addMessage("Expected parsing to " + successString(expected.success, false) + ", but it " + successString(actualSuccess, true), Severity.Error, file, sourceRegion);
            }
            if(actualSuccess) {
                expected.ambiguous.ifSome(ambiguous -> {
                    if(ambiguous != r.ambiguous) {
                        addParseMessages[0] = true;
                        messagesBuilder.addMessage("Expected " + ambiguousString(ambiguous, false) + " parse, but it parsed " + ambiguousString(r.ambiguous, true), Severity.Error, file, sourceRegion);
                    }
                });
                expected.recovered.ifSome(recovered -> {
                    if(recovered != r.recovered) {
                        addParseMessages[0] = true;
                        messagesBuilder.addMessage("Expected parsing to " + recoveredString(recovered, false) + ", but it " + recoveredString(r.recovered, true), Severity.Error, file, sourceRegion);
                    }
                });
            }
            if(addParseMessages[0]) {
                MessageRemap.addMessagesRemapped(messagesBuilder, testCase.resource, file, r.messages);
            }
        }, e -> {
            messagesBuilder.addMessage("Failed to evaluate parse expectation; see exception", e, Severity.Error, file, sourceRegion);
        });
        return messagesBuilder.build(file);
    }

    private String successString(boolean success, boolean pastTense) {
        return success ? (pastTense ? "succeeded" : "succeed") : (pastTense ? "failed" : "fail");
    }

    private String ambiguousString(boolean ambiguous, boolean pastTense) {
        return ambiguous ? (pastTense ? "ambiguously" : "ambiguous") : (pastTense ? "unambiguously" : "unambiguous");
    }

    private String recoveredString(boolean recovered, boolean pastTense) {
        return recovered ? (pastTense ? "recovered" : "recover") : (pastTense ? "did not recover" : "not recover");
    }
}
