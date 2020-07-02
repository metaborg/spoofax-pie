package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti;
import mb.tiger.spoofax.task.reusable.TigerParse;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @implNote Although Tiger is a single-file language, we implement the multi-file check variant here as well for
 * development/testing purposes.
 */
@LanguageScope
public class TigerIdeCheckMulti implements TaskDef<TigerIdeCheckMulti.Input, KeyedMessages> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourceWalker walker;
        public final ResourceMatcher matcher;

        public Input(
            ResourcePath root,
            ResourceWalker walker,
            ResourceMatcher matcher
        ) {
            this.root = root;
            this.walker = walker;
            this.matcher = matcher;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return root.equals(input.root) && walker.equals(input.walker) && matcher.equals(input.matcher);
        }

        @Override public int hashCode() {
            return Objects.hash(root, walker, matcher);
        }

        @Override public String toString() {
            return "Input{" +
                "root=" + root +
                ", walker=" + walker +
                ", matcher=" + matcher +
                '}';
        }
    }

    private final TigerParse parse;
    private final TigerAnalyzeMulti analyze;

    @Inject public TigerIdeCheckMulti(TigerParse parse, TigerAnalyzeMulti analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));
        try {
            root.walk(input.walker, input.matcher).forEach(file -> {
                final ResourcePath filePath = file.getPath();
                try {
                    final Messages messages = context.require(parse.createMessagesSupplier(filePath));
                    messagesBuilder.addMessages(filePath, messages);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final TigerAnalyzeMulti.Input analyzeInput = new TigerAnalyzeMulti.Input(input.root, input.walker, input.matcher, parse.createRecoverableAstFunction());
        final Result<ConstraintAnalyzeMultiTaskDef.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("Project-wide analysis failed", e, Severity.Error, input.root));
        return messagesBuilder.build();
    }
}
