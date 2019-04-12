package mb.tiger.spoofax.taskdef;

import mb.common.message.MessageCollection;
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

    @Override public @Nullable MessageCollection exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        final MessageCollection messageCollection = new MessageCollection();
        messageCollection.addDocumentMessages(key.toString(), parseOutput.messages);
        return messageCollection;
    }
}
