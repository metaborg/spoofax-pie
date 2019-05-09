package mb.tiger.spoofax.taskdef;

import mb.common.message.MessageCollection;
import mb.common.message.MessageCollectionBuilder;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class MessagesTaskDef implements TaskDef<ResourceKey, MessageCollection> {
    private final ParseTaskDef parseTaskDef;

    @Inject public MessagesTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public MessageCollection exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        final MessageCollectionBuilder builder = new MessageCollectionBuilder();
        builder.addMessages(parseOutput.messages);
        return builder.build();
    }
}
