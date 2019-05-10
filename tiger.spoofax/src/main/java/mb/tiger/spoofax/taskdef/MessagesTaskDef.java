package mb.tiger.spoofax.taskdef;

import mb.common.message.Messages;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;

import javax.inject.Inject;

public class MessagesTaskDef implements TaskDef<ResourceKey, Messages> {
    private final ParseTaskDef parseTaskDef;

    @Inject public MessagesTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Messages exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseOutput = context.require(parseTaskDef, key);
        return parseOutput.messages;
    }
}
