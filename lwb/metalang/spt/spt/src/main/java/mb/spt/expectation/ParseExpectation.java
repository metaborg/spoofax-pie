package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.parse.ParseResult;
import mb.spt.api.parse.TestableParse;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.util.SptMessageRemap;

public class ParseExpectation implements TestExpectation {
    public enum Recovery {
        NotRecovered,
        Recovered,
        DoNotCare,
    }

    public enum Ambiguity {
        Unambiguous,
        Ambiguous,
        DoNotCare,
    }

    private final boolean expectSuccess;
    private final Ambiguity expectAmbiguous;
    private final Recovery expectRecovered;
    private final Region sourceRegion;

    public ParseExpectation(boolean expectSuccess, Ambiguity expectAmbiguous, Recovery expectRecovered, Region sourceRegion) {
        this.expectSuccess = expectSuccess;
        this.expectRecovered = expectRecovered;
        this.expectAmbiguous = expectAmbiguous;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.testSuiteFile;
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableParse)) {
            messagesBuilder.addMessage("Cannot evaluate parse expectation because language instance '" + languageInstance + "' does not implement TestableParse", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableParse testableParse = (TestableParse)languageInstance;
        final Result<ParseResult, ?> result = testableParse.testParse(languageUnderTestSession, testCase.resource, testCase.rootDirectoryHint);
        result.ifElse(r -> {
            final boolean actualSuccess = r.success && !r.messages.containsError();
            boolean addParseMessages = false;
            if(expectSuccess != actualSuccess) {
                addParseMessages = true;
                messagesBuilder.addMessage("Expected parsing to " + successString(expectSuccess, false) + ", but it " + successString(actualSuccess, true), Severity.Error, file, sourceRegion);
            }
            if(actualSuccess) {
                switch(expectAmbiguous) {
                    case Unambiguous:
                        if(r.ambiguous) {
                            addParseMessages = true;
                            messagesBuilder.addMessage("Expected " + ambiguousString(false, false) + " parse, but it parsed " + ambiguousString(r.ambiguous, true), Severity.Error, file, sourceRegion);
                        }
                        break;
                    case Ambiguous:
                        if(!r.ambiguous) {
                            addParseMessages = true;
                            messagesBuilder.addMessage("Expected " + ambiguousString(true, false) + " parse, but it parsed " + ambiguousString(r.ambiguous, true), Severity.Error, file, sourceRegion);
                        }
                        break;
                    default:
                    case DoNotCare:
                        break;
                }
                switch(expectRecovered) {
                    case NotRecovered:
                        if(r.recovered) {
                            addParseMessages = true;
                            messagesBuilder.addMessage("Expected parsing to " + recoveredString(false, false) + " parse, but it " + recoveredString(r.ambiguous, true), Severity.Error, file, sourceRegion);
                        }
                        break;
                    case Recovered:
                        if(!r.recovered) {
                            addParseMessages = true;
                            messagesBuilder.addMessage("Expected parsing to " + recoveredString(true, false) + " parse, but it " + recoveredString(r.ambiguous, true), Severity.Error, file, sourceRegion);
                        }
                        break;
                    default:
                    case DoNotCare:
                        break;
                }
            }
            if(addParseMessages) {
                SptMessageRemap.addMessagesRemapped(messagesBuilder, testCase, file, sourceRegion, r.messages);
            }
        }, e -> {
            messagesBuilder.addMessage("Failed to parse test fragment; see exception", e, Severity.Error, file, sourceRegion);
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
