package mb.spt.task;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.spt.SptScope;

import javax.inject.Inject;

@SptScope
public class SptCheck implements TaskDef<SptConfig, KeyedMessages> {
    @Inject public SptCheck() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, SptConfig input) throws Exception {
        return null;
    }
}
