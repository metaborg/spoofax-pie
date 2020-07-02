package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class TigerIdeCheckAggregate implements TaskDef<TigerIdeCheckAggregate.Input, @Nullable KeyedMessages> {
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
            return root.equals(input.root) &&
                walker.equals(input.walker) &&
                matcher.equals(input.matcher);
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

    private final TigerIdeCheck check;

    @Inject public TigerIdeCheckAggregate(TigerIdeCheck check) {
        this.check = check;
    }

    @Override public String getId() {
        return TigerIdeCheckAggregate.class.getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) {
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        try {
            final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));
            root.walk(input.walker, input.matcher).forEach(file -> {
                final ResourceKey fileKey = file.getKey();
                final Messages messages = context.require(check, fileKey);
                builder.addMessages(fileKey, messages);
            });
        } catch(Exception e) {
            builder.addMessage("Aggregating messages for '" + input.root + "' failed", e, Severity.Error);
        }
        return builder.build(input.root);
    }
}

