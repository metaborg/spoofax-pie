package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.model.LanguageUnderTest;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;
import mb.spt.api.parse.ExpectedParseResult;
import mb.spt.api.parse.ParseResult;
import mb.spt.api.parse.TestableParse;

public class ParseExpectation implements TestExpectation {
    public final ExpectedParseResult expected;
    public final Region sourceRegion;

    public ParseExpectation(ExpectedParseResult expected, Region sourceRegion) {
        this.expected = expected;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(LanguageUnderTest languageUnderTest, Session session, TestCase testCase) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.file;
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableParse)) {
            messagesBuilder.addMessage("Cannot evaluate parse expectation because language instance '" + languageInstance + "' does not implement TestableParse", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableParse testableParse = (TestableParse)languageInstance;
        final Result<ParseResult, ?> result = testableParse.testParse(session, testCase);
        result.ifElse(r -> {
            final boolean actualSuccess = r.success && !r.messages.containsError();
            if(expected.success != actualSuccess) {
                messagesBuilder.addMessage("Expected parsing to " + successString(expected.success, false) + ", but it " + successString(actualSuccess, true), Severity.Error, file, sourceRegion);
                messagesBuilder.addMessages(r.messages);
            }
            if(actualSuccess) {
                expected.ambiguous.ifSome(ambiguous -> {
                    if(ambiguous != r.ambiguous) {
                        messagesBuilder.addMessage("Expected " + ambiguousString(ambiguous, false) + " parse, but it parsed " + ambiguousString(r.ambiguous, true), Severity.Error, file, sourceRegion);
                        messagesBuilder.addMessages(r.messages);
                    }
                });
                expected.recovered.ifSome(recovered -> {
                    if(recovered != r.recovered) {
                        messagesBuilder.addMessage("Expected parsing to " + recoveredString(recovered, false) + ", but it " + recoveredString(r.recovered, true), Severity.Error, file, sourceRegion);
                        messagesBuilder.addMessages(r.messages);
                    }
                });
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
