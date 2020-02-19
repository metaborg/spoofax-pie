package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.util.UncheckedException;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
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
        return "mb.tiger.spoofax.task.TigerCheck";
    }

    @Override
    public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));
        try {
            root.walk(input.walker, input.matcher).forEach(file -> {
                final ResourcePath filePath = file.getPath();
                try {
                    final JSGLR1ParseResult parseResult = context.require(parse, new ResourceStringSupplier(filePath));
                    messagesBuilder.addMessages(filePath, parseResult.getMessages());
                } catch(ExecException | InterruptedException e) {
                    throw new UncheckedException(e);
                }
            });
        } catch(UncheckedException e) {
            throw e.getCause();
        }

        final TigerAnalyzeMulti.Input analyzeInput = new TigerAnalyzeMulti.Input(input.root, input.walker, input.matcher, parse.createAstFunction());
        final TigerAnalyzeMulti.@Nullable Output analysisOutput = context.require(analyze, analyzeInput);
        if(analysisOutput != null) {
            messagesBuilder.addMessages(analysisOutput.result.messages);
        }
        return messagesBuilder.build();
    }
}
