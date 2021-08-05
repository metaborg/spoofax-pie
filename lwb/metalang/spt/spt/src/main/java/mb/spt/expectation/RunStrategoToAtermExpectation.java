package mb.spt.expectation;

import mb.aterm.common.TermToString;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.util.SptAtermMatcher;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;

public class RunStrategoToAtermExpectation extends RunStrategoExpectation {
    private final IStrategoTerm expectedMatch;

    public RunStrategoToAtermExpectation(
        String strategyName,
        Option<SelectionReference> selection,
        IStrategoTerm expectedMatch,
        Region sourceRegion
    ) {
        super(strategyName, ListView.of(), selection, sourceRegion, false);
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
