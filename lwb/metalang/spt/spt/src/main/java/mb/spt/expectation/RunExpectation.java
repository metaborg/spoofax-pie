package mb.spt.expectation;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Session;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageInstance;
import mb.spt.api.analyze.RunArgument;
import mb.spt.api.analyze.TestableAnalysis;
import mb.spt.fromterm.InvalidAstShapeException;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.SelectionReference;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.stratego.common.StrategoException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;
import java.util.List;

public class RunExpectation implements TestExpectation {
    private final String strategy;
    private final Option<ListView<IStrategoAppl>> arguments;
    private final Option<SelectionReference> selectionReference;
    private final Region sourceRegion;
    private final boolean expectFailure;

    public RunExpectation(String strategy, Option<ListView<IStrategoAppl>> arguments, Option<SelectionReference> selectionReference, Region sourceRegion, boolean expectFailure) {
        this.strategy = strategy;
        this.arguments = arguments;
        this.selectionReference = selectionReference;
        this.sourceRegion = sourceRegion;
        this.expectFailure = expectFailure;
    }

    protected void checkAst(
        IStrategoTerm ast,
        KeyedMessagesBuilder messagesBuilder,
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        Region sourceRegion
    ) throws InterruptedException {}

    @Override
    public KeyedMessages evaluate(
        TestCase testCase,
        LanguageUnderTest languageUnderTest,
        Session languageUnderTestSession,
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        CancelToken cancel
    ) throws InterruptedException {
        final ResourceKey file = testCase.testSuiteFile;
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final LanguageInstance languageInstance = languageUnderTest.getLanguageComponent().getLanguageInstance();
        if(!(languageInstance instanceof TestableAnalysis)) {
            messagesBuilder.addMessage("Cannot evaluate run expectation because language instance '" + languageInstance + "' does not implement TestableAnalysis", Severity.Error, file, sourceRegion);
            return messagesBuilder.build(file);
        }
        final TestableAnalysis testableAnalysis = (TestableAnalysis)languageInstance;
        Option<Region> region = selectionReference.map(
            (sel) -> testCase.testFragment.getSelections().get(sel.selection - 1)
        );
        final Result<IStrategoTerm, ?> result = testableAnalysis.testRunStrategy(languageUnderTestSession, testCase.resource, strategy, parseArguments(arguments, testCase), region, testCase.rootDirectoryHint);

        result
            .ifOkThrowing(
                (o) -> {
                    if (expectFailure) {
                        messagesBuilder.addMessage("Expected strategy to fail, but it succeeded", Severity.Error, file, sourceRegion);
                    }
                    checkAst(o, messagesBuilder, testCase, languageUnderTest, languageUnderTestSession, languageUnderTestProvider, context, sourceRegion);
                }
            )
            .ifErr(
                (e) -> {
                    if (e instanceof StrategoException) {
                        if(!expectFailure) {
                            messagesBuilder.addMessage(
                                ((StrategoException)e).getMessage(),
                                e,
                                Severity.Error,
                                file,
                                sourceRegion
                            );
                        }
                    } else {
                        messagesBuilder.extractMessagesRecursively(e);
                    }
                }
            );

        return messagesBuilder.build();
    }

    private Option<ListView<RunArgument>> parseArguments(Option<ListView<IStrategoAppl>> arguments, TestCase testCase) {
        return arguments.map((terms) -> {
            List<RunArgument> args = new ArrayList<>();
            for(IStrategoAppl term : terms) {
                switch(term.getName()) {
                    case "Int": {
                        IStrategoInt value = TermUtils.toIntAt(term, 0);
                        args.add(RunArgument.intArg(value));
                        break;
                    }
                    case "String": {
                        IStrategoString value = TermUtils.toStringAt(term, 0);
                        args.add(RunArgument.stringArg(value));
                        break;
                    }
                    case "SelectionRef": {
                        int index = Integer.parseInt(TermUtils.toJavaStringAt(term, 0));
                        Region region = testCase.testFragment.getSelections().get(index - 1);
                        args.add(RunArgument.selectionArg(region));
                        break;
                    }
                    default: {
                        throw new InvalidAstShapeException("Int/1, String/1 or SelectionRef/1", term);
                    }
                }
            }
            return ListView.of(args);
        });
    }
}
