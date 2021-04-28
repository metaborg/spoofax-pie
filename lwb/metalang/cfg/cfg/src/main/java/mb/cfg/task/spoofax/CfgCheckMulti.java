package mb.cfg.task.spoofax;

import mb.cfg.CfgClassLoaderResources;
import mb.cfg.CfgScope;
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
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

@CfgScope
public class CfgCheckMulti implements TaskDef<ResourcePath, KeyedMessages> {
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

    @Override public KeyedMessages exec(ExecContext context, ResourcePath input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(walker, ResourceMatcher.ofDirectory(), context::require);
        final ResourceMatcher matcher = ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtensions("cfg")));
        try {
            rootDirectory.walkForEach(walker, matcher, file -> {
                final ResourcePath filePath = file.getPath();
                final Messages messages = context.require(parse.inputBuilder().withFile(filePath).rootDirectoryHint(input).buildMessagesSupplier());
                messagesBuilder.addMessages(filePath, messages);
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = context.require(rootDirectoryToObject, input);
        result.ifOk(o -> messagesBuilder.addMessages(o.messages));
        result.ifErr(e -> {
            messagesBuilder.addMessage("Creating configuration object failed", e, Severity.Error, e.getCfgFile());
            messagesBuilder.extractMessagesRecursivelyWithFallbackKey(e, e.getCfgFile());
        });
        return messagesBuilder.build();
    }
}
