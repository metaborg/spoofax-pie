package mb.spt.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.common.util.MapView;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spt.fromterm.TestExpectationFromTerm;
import mb.spt.lut.LanguageUnderTestProviderWrapper;
import mb.spt.SptClassLoaderResources;
import mb.spt.SptScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Optional;

@SptScope
public class SptCheck implements TaskDef<SptCheck.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final ResourceKey file;
        public final @Nullable ResourcePath rootDirectoryHint;

        public Input(ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
            this.file = file;
            this.rootDirectoryHint = rootDirectoryHint;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!file.equals(input.file)) return false;
            return rootDirectoryHint != null ? rootDirectoryHint.equals(input.rootDirectoryHint) : input.rootDirectoryHint == null;
        }

        @Override public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "SptCheckWrapper$Input{" +
                "file=" + file +
                ", rootDirectoryHint=" + rootDirectoryHint +
                '}';
        }
    }


    private final SptClassLoaderResources classLoaderResources;
    private final SptParse parse;
    private final LanguageUnderTestProviderWrapper wrapper;
    private final MapView<IStrategoConstructor, TestExpectationFromTerm> testExpectationFromTerms;


    @Inject public SptCheck(
        SptClassLoaderResources classLoaderResources,
        SptParse parse,
        LanguageUnderTestProviderWrapper wrapper,
        MapView<IStrategoConstructor, TestExpectationFromTerm> testExpectationFromTerms
    ) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.wrapper = wrapper;
        this.testExpectationFromTerms = testExpectationFromTerms;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final mb.jsglr.pie.JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().withFile(input.file).rootDirectoryHint(Optional.ofNullable(input.rootDirectoryHint));
        final Result<JsglrParseOutput, JsglrParseException> parseResult = context.require(parse, parseInputBuilder.build());
        parseResult.ifElse(o -> {
                messagesBuilder.addMessages(o.messages);
                runTests(context, messagesBuilder, input.file, input.rootDirectoryHint, o.ast);
            },
            messagesBuilder::extractMessagesRecursively
        );
        return messagesBuilder.build();
    }


    private void runTests(
        ExecContext context,
        KeyedMessagesBuilder messagesBuilder,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        IStrategoTerm ast
    ) {
        // TODO: extract language id hint from AST.
        final Result<LanguageComponent, ?> languageUnderTestResult = wrapper.get().provide(context, file, rootDirectoryHint, null);
        languageUnderTestResult.ifElse(lc -> runTests(context, lc, messagesBuilder, file, rootDirectoryHint, ast), messagesBuilder::extractMessagesRecursively);
    }

    private void runTests(
        ExecContext context,
        LanguageComponent languageComponent,
        KeyedMessagesBuilder messagesBuilder,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        IStrategoTerm ast
    ) {
        // TODO: run tests.
    }
}
