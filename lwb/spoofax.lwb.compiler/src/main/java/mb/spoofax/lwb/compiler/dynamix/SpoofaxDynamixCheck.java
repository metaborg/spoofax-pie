package mb.spoofax.lwb.compiler.dynamix;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.dynamix.task.DynamixCheckMulti;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;

/**
 * Check task for Dynamix in the context of the Spoofax LWB compiler.
 */
public class SpoofaxDynamixCheck implements TaskDef<ResourcePath, KeyedMessages> {
    private final SpoofaxDynamixConfigure configure;
    private final DynamixCheckMulti check;

    @Inject public SpoofaxDynamixCheck(
        SpoofaxDynamixConfigure configure,
        DynamixCheckMulti check
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
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check Dynamix because it could not be configured", e)), rootDirectory))
        );
    }

    public KeyedMessages check(ExecContext context, SpoofaxDynamixConfig config) {
        return config.caseOf()
            .files((dynamixConfig, outputSpecAtermDirectory) -> context.require(check, dynamixConfig.rootDirectory))
            .prebuilt((inputSpecAtermDirectory, outputSpecAtermDirectory) -> KeyedMessages.of())
            ;
    }
}
