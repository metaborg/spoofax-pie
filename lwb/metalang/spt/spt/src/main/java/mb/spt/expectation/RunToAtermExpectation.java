package mb.spt.expectation;

import mb.aterm.common.TermToString;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.parse.TestableParse;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.util.SptAtermMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.Term;
import org.spoofax.terms.TermFactory;

public class RunToAtermExpectation extends RunExpectation {
    private final IStrategoTerm expectedMatch;

    public RunToAtermExpectation(
        String strategyName,
        Option<SelectionReference> selection,
        IStrategoTerm expectedMatch,
        Region sourceRegion
    ) {
        super(strategyName, selection, sourceRegion, false);
        this.expectedMatch = expectedMatch;
    }

    @Override
    protected void checkAst(
        IStrategoTerm ast,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession, LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        Region sourceRegion
    ) {
        if (!SptAtermMatcher.check(ast, expectedMatch, new TermFactory())) {
            messagesBuilder.addMessage(
                "Expected run to " + SptAtermMatcher.prettyPrint(expectedMatch) + ", but got " + TermToString.toString(ast),
                Severity.Error,
                testCase.testSuiteFile,
                sourceRegion
            );
        }
    }
}
