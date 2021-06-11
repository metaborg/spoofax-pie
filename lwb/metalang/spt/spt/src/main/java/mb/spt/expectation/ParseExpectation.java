package mb.spt.expectation;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.parse.ParseResult;
import mb.spt.api.parse.TestableParse;
import mb.spt.api.model.TestCase;
import mb.spt.api.model.TestExpectation;

public class ParseExpectation implements TestExpectation {
    public final ParseResult expected;
    public final Region sourceRegion;

    public ParseExpectation(ParseResult expected, Region sourceRegion) {
        this.expected = expected;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public boolean evaluate(ExecContext context, LanguageComponent languageComponent, KeyedMessagesBuilder messagesBuilder, TestCase testCase) {
        final ResourceKey file = testCase.file;
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        if(!(languageInstance instanceof TestableParse)) {
            messagesBuilder.addMessage("Cannot evaluate parse expectation because language instance '" + languageInstance + "' does not implement TestableParse", Severity.Error, file, sourceRegion);
            return false;
        }
        final TestableParse testableParse = (TestableParse)languageInstance;
        final Result<ParseResult, ?> result = testableParse.testParse(context, testCase);
        return result.mapOrElse(r -> {
            if(expected.success != r.success) {
                messagesBuilder.addMessage("Expected parsing to " + successString(expected, false) + ", but it " + successString(r, true), Severity.Error, file, sourceRegion);
                return false;
            }
            if(expected.ambiguous != r.ambiguous) {
                messagesBuilder.addMessage("Expected " + ambiguousString(expected, false) + " parse, but it parsed " + ambiguousString(r, true), Severity.Error, file, sourceRegion);
                return false;
            }
            if(expected.recovered != r.recovered) {
                messagesBuilder.addMessage("Expected parsing to " + recoveredString(expected, false) + ", but it " + recoveredString(r, true), Severity.Error, file, sourceRegion);
                return false;
            }
            return true;
        }, e -> {
            messagesBuilder.addMessage("Failed to evaluate parse expectation; see exception", e, Severity.Error, file, sourceRegion);
            return false;
        });
    }

    private String successString(ParseResult result, boolean pastTense) {
        return result.success ? (pastTense ? "succeeded" : "succeed") : (pastTense ? "failed" : "fail");
    }

    private String ambiguousString(ParseResult result, boolean pastTense) {
        return result.ambiguous ? (pastTense ? "ambiguously" : "ambiguous") : (pastTense ? "unambiguously" : "unambiguous");
    }

    private String recoveredString(ParseResult result, boolean pastTense) {
        return result.recovered ? (pastTense ? "recovered" : "recover") : (pastTense ? "did not recover" : "not recover");
    }
}
