package mb.tiger.spoofax.taskdef;

import mb.common.message.MessageCollection;
import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.tiger.spoofax.taskdef.ParseTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class MessagesTaskDef implements TaskDef<FSPath, @Nullable MessageCollection> {
    private final ParseTaskDef parseTaskDef;

    @Inject public MessagesTaskDef(ParseTaskDef parseTaskDef) {
        this.parseTaskDef = parseTaskDef;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public @Nullable MessageCollection exec(ExecContext context, FSPath path) throws Exception {
        final @Nullable JSGLR1ParseOutput parseOutput = context.require(parseTaskDef, path);
        if(parseOutput == null) {
            return null;
        }
        final MessageCollection messageCollection = new MessageCollection();
        messageCollection.addDocumentMessages(path.toString(), parseOutput.messages);
        return messageCollection;
    }
}
