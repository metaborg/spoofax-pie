package mb.tiger.spoofax.taskdef;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;

import javax.inject.Inject;

public class MessagesTaskDef implements TaskDef<ResourceKey, KeyedMessages> {
    private final ParseTaskDef parseTaskDef;

    @Inject public MessagesTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        builder.addMessages(key, parseOutput.messages);
        return builder.build();
    }
}
