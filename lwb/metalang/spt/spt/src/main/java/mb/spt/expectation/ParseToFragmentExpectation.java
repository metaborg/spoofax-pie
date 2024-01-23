package mb.spt.expectation;

import mb.aterm.common.TermToString;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.MixedSession;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.parse.TestableParse;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ParseToFragmentExpectation implements TestExpectation {
    private final ResourceKey fragmentResource;
    private final @Nullable CoordinateRequirement languageCoordinateRequirementHint;
    private final Region sourceRegion;

    public ParseToFragmentExpectation(ResourceKey fragmentResource, @Nullable CoordinateRequirement languageCoordinateRequirementHint, Region sourceRegion) {
        this.fragmentResource = fragmentResource;
        this.languageCoordinateRequirementHint = languageCoordinateRequirementHint;
        this.sourceRegion = sourceRegion;
    }

    @Override
    public KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        @Nullable ResourcePath rootDirectoryHint,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceKey file = testCase.testSuiteFile;

        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableParse)) {
            messagesBuilder.addMessage("Cannot evaluate parse to fragment expectation because language instance '" + languageInstance + "' does not implement TestableParse", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableParse testableParse = (TestableParse)languageInstance;

        final @Nullable LanguageUnderTest fragmentLanguageUnderTest = ExpectationFragmentUtil.getLanguageUnderTest(testCase, languageUnderTest, languageUnderTestProvider, context, languageCoordinateRequirementHint);
        if(fragmentLanguageUnderTest == null) {
            messagesBuilder.addMessage("Cannot evaluate parse to fragment expectation because providing language under test for language '" + languageCoordinateRequirementHint + "' failed unexpectedly", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }

        final LanguageInstance fragmentLanguageInstance = fragmentLanguageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(fragmentLanguageInstance instanceof TestableParse)) {
            messagesBuilder.addMessage("Cannot evaluate parse to fragment expectation because fragment language instance '" + fragmentLanguageInstance + "' does not implement TestableParse", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableParse fragmentTestableParse = (TestableParse)fragmentLanguageInstance;

        final Result<IStrategoTerm, ?> result = testableParse.testParseToAterm(languageUnderTestSession, testCase.resource, testCase.rootDirectoryHint);
        final Result<IStrategoTerm, ?> fragmentResult;
        try (final MixedSession session = fragmentLanguageUnderTest.getPieComponent().newSession()) { // OPTO: share a single session for one test suite run.
            fragmentResult = fragmentTestableParse.testParseToAterm(
                session,
                fragmentResource,
                testCase.rootDirectoryHint
            );
        }
        result.ifElse(r -> {
            fragmentResult.ifElse(f -> {
                if(!r.equals(f)) {
                    messagesBuilder.addMessage("Expected parse to " + TermToString.toString(f) + ", but got " + TermToString.toString(r), Severity.Error, file, sourceRegion);
                }
            }, e -> {
                messagesBuilder.addMessage("Failed to parse expectation fragment; see exception", e, Severity.Error, file, sourceRegion);
            });
        }, e -> {
            messagesBuilder.addMessage("Failed to parse test fragment; see exception", e, Severity.Error, file, sourceRegion);
        });
        return messagesBuilder.build(file);
    }
}
