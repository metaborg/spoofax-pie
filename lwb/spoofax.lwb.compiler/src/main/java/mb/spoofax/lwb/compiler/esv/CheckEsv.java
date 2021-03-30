package mb.spoofax.lwb.compiler.esv;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.esv.task.EsvCheck;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;

public class CheckEsv implements TaskDef<ResourcePath, KeyedMessages> {
    private final ConfigureEsv configure;
    private final EsvCheck check;

    @Inject public CheckEsv(
        ConfigureEsv configure,
        EsvCheck check
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
                c -> context.require(check, c),
                KeyedMessages::of
            ),
            e -> KeyedMessages.ofTryExtractMessagesFrom(e, rootDirectory)
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check ESV because it could not be configured", e)), rootDirectory))
        );
    }
}
