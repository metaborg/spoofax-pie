package mb.cfg.task.spoofax;

import mb.cfg.CfgClassLoaderResources;
import mb.cfg.task.CfgParse;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Objects;

@mb.cfg.CfgScope
public class CfgCheckMulti implements TaskDef<CfgCheckMulti.Input, KeyedMessages> {
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

    private final CfgClassLoaderResources classLoaderResources;
    private final CfgParse parse;
    private final CfgRootDirectoryToObject rootDirectoryToObject;

    @Inject public CfgCheckMulti(
        CfgClassLoaderResources classLoaderResources,
        CfgParse parse,
        CfgRootDirectoryToObject rootDirectoryToObject
    ) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.rootDirectoryToObject = rootDirectoryToObject;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final HierarchicalResource root = context.require(input.root, ResourceStampers.modifiedDirRec(input.walker, input.matcher));
        try {
            root.walk(input.walker, input.matcher).forEach(file -> {
                final ResourcePath filePath = file.getPath();
                final Messages messages = context.require(parse.createMessagesSupplier(filePath));
                messagesBuilder.addMessages(filePath, messages);
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = context.require(rootDirectoryToObject, input.root);
        result.ifOk(o -> messagesBuilder.addMessages(o.messages));
        result.ifErr(e -> {
            messagesBuilder.addMessage("Creating configuration object failed", e, Severity.Error, e.getCfgFile());
            messagesBuilder.addMessagesRecursivelyWithFallbackKey(e, e.getCfgFile());
        });
        return messagesBuilder.build();
    }
}
