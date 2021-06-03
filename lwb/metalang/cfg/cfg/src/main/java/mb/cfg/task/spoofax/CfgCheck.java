package mb.cfg.task.spoofax;

import mb.cfg.CfgClassLoaderResources;
import mb.cfg.CfgScope;
import mb.cfg.task.CfgAnalyze;
import mb.cfg.task.CfgParse;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@CfgScope
public class CfgCheck implements TaskDef<CfgCheck.Input, KeyedMessages> {
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
            final CfgCheck.Input input = (CfgCheck.Input)o;
            if(!file.equals(input.file)) return false;
            return rootDirectoryHint != null ? rootDirectoryHint.equals(input.rootDirectoryHint) : input.rootDirectoryHint == null;
        }

        @Override public int hashCode() {
            int result = file.hashCode();
            result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "CfgCheck$Input{" +
                "file=" + file +
                ", rootDirectoryHint=" + rootDirectoryHint +
                '}';
        }
    }

    private final CfgClassLoaderResources classLoaderResources;
    private final CfgParse parse;
    private final CfgAnalyze analyze;
    private final CfgRootDirectoryToObject rootDirectoryToObject;

    @Inject public CfgCheck(
        CfgClassLoaderResources classLoaderResources,
        CfgParse parse,
        CfgAnalyze analyze,
        CfgRootDirectoryToObject rootDirectoryToObject
    ) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.analyze = analyze;
        this.rootDirectoryToObject = rootDirectoryToObject;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().withFile(input.file).rootDirectoryHint(Optional.ofNullable(input.rootDirectoryHint));
        final Messages parseMessages = context.require(parseInputBuilder.buildMessagesSupplier());
        messagesBuilder.addMessages(input.file, parseMessages);

        final AtomicBoolean analysisErrors = new AtomicBoolean(false);
        final Result<CfgAnalyze.Output, ?> analysisResult = context.require(analyze, new CfgAnalyze.Input(input.file, parseInputBuilder.buildRecoverableAstSupplier()));
        analysisResult
            .ifOk(output -> {
                analysisErrors.set(output.result.messages.containsError());
                messagesBuilder.addMessages(output.result.resource, output.result.messages);
            })
            .ifErr(e -> {
                analysisErrors.set(true);
                messagesBuilder.addMessage("Analysis failed", e, Severity.Error);
            });

        if(!analysisErrors.get() && input.rootDirectoryHint != null) {
            final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = context.require(rootDirectoryToObject, input.rootDirectoryHint);
            result.ifOk(o -> messagesBuilder.addMessages(o.messages));
            result.ifErr(e -> {
                messagesBuilder.addMessage("Creating configuration object failed", e, Severity.Error, e.getCfgFile());
                messagesBuilder.extractMessagesRecursivelyWithFallbackKey(e, e.getCfgFile());
            });
        }

        return messagesBuilder.build();
    }
}
