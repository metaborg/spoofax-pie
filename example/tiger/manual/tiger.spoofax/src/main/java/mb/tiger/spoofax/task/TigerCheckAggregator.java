package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;
import java.io.IOException;

@TigerScope
public class TigerCheckAggregator implements TaskDef<ResourcePath, KeyedMessages> {
    private final TigerCheck check;

    @Inject public TigerCheckAggregator(TigerCheck check) {
        this.check = check;
    }

    @Override public String getId() {
        return TigerCheckAggregator.class.getName();
    }

    @Override public KeyedMessages exec(ExecContext context, ResourcePath input) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(walker, ResourceMatcher.ofDirectory(), context::require);
        final ResourceMatcher matcher = ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtensions("tig")));
        rootDirectory.walkForEach(walker, matcher, file -> {
            final ResourceKey fileKey = file.getKey();
            final KeyedMessages messages = context.require(check, new TigerCheck.Input(fileKey, input));
            messagesBuilder.addMessages(messages);
        });
        return messagesBuilder.build();
    }
}

