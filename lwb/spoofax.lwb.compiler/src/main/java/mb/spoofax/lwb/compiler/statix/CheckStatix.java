package mb.spoofax.lwb.compiler.statix;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixCheckMulti;

import javax.inject.Inject;

public class CheckStatix implements TaskDef<ResourcePath, KeyedMessages> {
    private final ConfigureStatix configure;
    private final StatixCheckMulti check;

    @Inject public CheckStatix(
        ConfigureStatix configure,
        StatixCheckMulti check
    ) {
        this.configure = configure;
        this.check = check;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath rootDirectory) {
        return context.require(configure, rootDirectory).mapOrElse(
            o -> o.mapOrElse(
                c -> context.require(check, c.rootDirectory), // TODO: remove this task, as check just accepts a root directory?
                KeyedMessages::of
            ),
            e -> KeyedMessages.ofTryExtractMessagesFrom(e, rootDirectory)
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check Statix because it could not be configured", e)), rootDirectory))
        );
    }
}
