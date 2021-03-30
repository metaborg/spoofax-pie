package mb.spoofax.lwb.compiler.stratego;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.str.task.StrategoCheck;

import javax.inject.Inject;

public class CheckStratego implements TaskDef<ResourcePath, KeyedMessages> {
    private final ConfigureStratego configure;
    private final StrategoCheck check;

    @Inject public CheckStratego(
        ConfigureStratego configure,
        StrategoCheck check
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
                c -> context.require(check, c.toAnalyzeConfig()),
                KeyedMessages::of
            ),
            e -> KeyedMessages.ofTryExtractMessagesFrom(e, rootDirectory)
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check Stratego because it could not be configured", e)), rootDirectory))
        );
    }
}
