package mb.spt.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.testrunner.TestCaseResult;
import mb.spoofax.core.language.testrunner.TestSuiteResult;
import mb.spt.SptClassLoaderResources;
import mb.spt.SptScope;
import mb.spt.fromterm.FromTermException;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.fromterm.TestSuiteFromTerm;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.lut.LanguageUnderTestProviderWrapper;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.TestCase;
import mb.spt.model.TestExpectation;
import mb.spt.model.TestSuite;
import mb.spt.resource.SptTestCaseResourceRegistry;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SptScope
public class SptRunTestSuite implements TaskDef<SptRunTestSuite.Input, TestSuiteResult> {
    public static class Input implements Serializable {
        public final ResourceKey file;
        public final @Nullable ResourcePath rootDirectoryHint;

        public Input(ResourceKey file) {
            this.file = file;
            this.rootDirectoryHint = null;
        }

        public Input(ResourceKey file, ResourcePath rootDirectoryHint) {
            this.file = file;
            this.rootDirectoryHint = rootDirectoryHint;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!file.equals(input.file)) return false;
            return Objects.equals(rootDirectoryHint, input.rootDirectoryHint);
        }

        @Override public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "SptRunTestSuite$Input{" +
                "file=" + file +
                ", rootDirectoryHint=" + rootDirectoryHint +
                '}';
        }
    }


    private final SptClassLoaderResources classLoaderResources;
    private final SptTestCaseResourceRegistry testCaseResourceRegistry;
    private final SptParse parse;
    private final SptGetStrategoRuntimeProvider getStrategoRuntimeProvider;
    private final LanguageUnderTestProviderWrapper wrapper;
    private final MapView<IStrategoConstructor, TestExpectationFromTerm> testExpectationFromTerms;


    @Inject public SptRunTestSuite(
        SptClassLoaderResources classLoaderResources,
        SptTestCaseResourceRegistry testCaseResourceRegistry,
        SptParse parse,
        SptGetStrategoRuntimeProvider getStrategoRuntimeProvider,
        LanguageUnderTestProviderWrapper wrapper,
        MapView<IStrategoConstructor, TestExpectationFromTerm> testExpectationFromTerms
    ) {
        this.classLoaderResources = classLoaderResources;
        this.testCaseResourceRegistry = testCaseResourceRegistry;
        this.parse = parse;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.wrapper = wrapper;
        this.testExpectationFromTerms = testExpectationFromTerms;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TestSuiteResult exec(ExecContext context, Input input) throws IOException, InterruptedException {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final mb.jsglr.pie.JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().withFile(input.file).rootDirectoryHint(Optional.ofNullable(input.rootDirectoryHint));
        final Result<JsglrParseOutput, JsglrParseException> parseResult = context.require(parse, parseInputBuilder.build());
        return parseResult.mapThrowingOrElse(o -> {
            messagesBuilder.addMessages(o.messages);
            return runTests(context, messagesBuilder, input.file, input.rootDirectoryHint, o.ast);
        }, (e) -> {
            messagesBuilder.extractMessagesRecursively(e);
            return new TestSuiteResult(messagesBuilder.build(), input.file);
        });
    }


    private TestSuiteResult runTests(
        ExecContext context,
        KeyedMessagesBuilder messagesBuilder,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        IStrategoTerm ast
    ) throws InterruptedException {
        final IStrategoTerm desugaredAst;
        try {
            final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
            desugaredAst = strategoRuntime.invoke("desugar-before", ast);
        } catch(StrategoException e) {
            messagesBuilder.extractMessagesRecursivelyWithFallbackKey(e, file);
            return new TestSuiteResult(messagesBuilder.build(), file);
        }

        final TestSuite testSuite;
        try {
            testSuite = TestSuiteFromTerm.testSuiteFromTerm(desugaredAst, testExpectationFromTerms, testCaseResourceRegistry, file, rootDirectoryHint);
        } catch(FromTermException e) {
            messagesBuilder.extractMessagesRecursivelyWithFallbackKey(e, file);
            return new TestSuiteResult(messagesBuilder.build(), file);
        }

        final LanguageUnderTestProvider languageUnderTestProvider = wrapper.get();
        final Result<LanguageUnderTest, ?> languageUnderTestResult = languageUnderTestProvider.provide(context, file, rootDirectoryHint, testSuite.languageCoordinateRequirementHint);
        final CancelToken cancelToken = context.cancelToken();
        return languageUnderTestResult.mapThrowingOrElse(
            languageUnderTest -> {
                ListView<TestCaseResult> results = runTests(languageUnderTestProvider, context, languageUnderTest, cancelToken, testSuite);
                return new TestSuiteResult(messagesBuilder.build(), file, testSuite.name, results);
            },
            (e) -> {
                messagesBuilder.extractMessagesRecursively(e);
                return new TestSuiteResult(messagesBuilder.build(), file, testSuite.name, ListView.of());
            }
        );
    }

    private ListView<TestCaseResult> runTests(
        LanguageUnderTestProvider languageUnderTestProvider,
        ExecContext context,
        LanguageUnderTest languageUnderTest,
        CancelToken cancelToken,
        TestSuite testSuite
    ) throws InterruptedException {
        List<TestCaseResult> results = new ArrayList<>();
        try(final MixedSession languageUnderTestSession = languageUnderTest.getPieComponent().newSession()) {
            for(TestCase testCase : testSuite.testCases) {
                context.cancelToken().throwIfCanceled();
                final KeyedMessagesBuilder testMessageBuilder = new KeyedMessagesBuilder();
                final long startTime = System.currentTimeMillis();
                for(TestExpectation expectation : testCase.expectations) {
                    testMessageBuilder.addMessages(
                        expectation.evaluate(testCase, languageUnderTest, languageUnderTestSession, languageUnderTestProvider, context, cancelToken)
                    );
                }
                final long duration = System.currentTimeMillis() - startTime;
                final KeyedMessages messages = testMessageBuilder.build();
                final TestCaseResult run = new TestCaseResult(testCase.description, testCase.descriptionRegion, testSuite.file, messages, duration);
                results.add(run);
            }
        }
        return ListView.of(results);
    }
}
