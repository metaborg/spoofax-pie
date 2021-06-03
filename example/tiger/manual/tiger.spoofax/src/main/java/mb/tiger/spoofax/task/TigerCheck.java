package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@TigerScope
public class TigerCheck implements TaskDef<TigerCheck.Input, KeyedMessages> {
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
            return "TigerCheck$Input{" +
                "file=" + file +
                ", rootDirectoryHint=" + rootDirectoryHint +
                '}';
        }
    }

    private final TigerParse parse;
    private final TigerAnalyze analyze;

    @Inject public TigerCheck(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, TigerCheck.Input input) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().withFile(input.file).rootDirectoryHint(Optional.ofNullable(input.rootDirectoryHint));
        final Messages parseMessages = context.require(parseInputBuilder.buildMessagesSupplier());
        messagesBuilder.addMessages(input.file, parseMessages);
        final Result<TigerAnalyze.Output, ?> analysisResult = context.require(analyze, new TigerAnalyze.Input(input.file, parseInputBuilder.buildRecoverableAstSupplier()));
        analysisResult
            .ifOk(output -> messagesBuilder.addMessages(output.result.resource, output.result.messages))
            .ifErr(e -> messagesBuilder.addMessage("Analysis failed", e, Severity.Error));
        return messagesBuilder.build();
    }
}
