package mb.tiger.spoofax;

import mb.common.message.MessageCollection;
import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecException;
import mb.pie.api.exec.TopDownExecutor;
import mb.spoofax.core.language.MessageService;
import mb.tiger.spoofax.pie.ParseTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TigerMessageService implements MessageService {
    private final TopDownExecutor topDownExecutor;
    private final ParseTaskDef parseTaskDef;

    public TigerMessageService(TopDownExecutor topDownExecutor, ParseTaskDef parseTaskDef) {
        this.topDownExecutor = topDownExecutor;
        this.parseTaskDef = parseTaskDef;
    }

    @Override public @Nullable MessageCollection getMessages(FSPath path) {
        try {
            final @Nullable JSGLR1ParseOutput parseOutput =
                topDownExecutor.newSession().requireInitial(parseTaskDef.createTask(path));
            if(parseOutput == null) {
                return null;
            }
            final MessageCollection messageCollection = new MessageCollection();
            messageCollection.addDocumentMessages(path.toString(), parseOutput.messages);
            return messageCollection;
        } catch(ExecException e) {
            throw new RuntimeException("Getting messages for path '" + path + "' failed unexpectedly", e.getCause());
        }
    }
}
