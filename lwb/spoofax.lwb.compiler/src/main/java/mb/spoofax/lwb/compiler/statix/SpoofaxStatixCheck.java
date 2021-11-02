package mb.spoofax.lwb.compiler.statix;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixCheckMulti;

import javax.inject.Inject;

/**
 * Check task for Statix in the context of the Spoofax LWB compiler.
 */
public class SpoofaxStatixCheck implements TaskDef<ResourcePath, KeyedMessages> {
    private final SpoofaxStatixConfigure configure;
    private final StatixCheckMulti check;

    @Inject public SpoofaxStatixCheck(
        SpoofaxStatixConfigure configure,
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
                c -> check(context, c),
                KeyedMessages::of
            ),
            e -> KeyedMessages.ofTryExtractMessagesFrom(e, rootDirectory)
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check Statix because it could not be configured", e)), rootDirectory))
        );
    }

    public KeyedMessages check(ExecContext context, SpoofaxStatixConfig config) {
        return config.caseOf()
            .files((statixConfig, outputSpecAtermDirectory) -> context.require(check, statixConfig.rootDirectory))
            .prebuilt((inputSpecAtermDirectory, outputSpecAtermDirectory) -> KeyedMessages.of())
            ;
    }
}
