package mb.spoofax.lwb.compiler.sdf3;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3CheckSpec;

import javax.inject.Inject;

/**
 * Check task for SDF3 in the context of the Spoofax LWB compiler.
 */
public class SpoofaxSdf3Check implements TaskDef<ResourcePath, KeyedMessages> {
    private final SpoofaxSdf3Configure configure;
    private final Sdf3CheckSpec check;

    @Inject public SpoofaxSdf3Check(
        SpoofaxSdf3Configure configure,
        Sdf3CheckSpec check
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
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check SDF3 because it could not be configured", e)), rootDirectory))
        );
    }

    public KeyedMessages check(ExecContext context, SpoofaxSdf3Config config) {
        return config.caseOf()
            .files((sdf3SpecConfig, outputParseTableAtermFile, outputParseTablePersistedFile) -> context.require(check, sdf3SpecConfig))
            .prebuilt((inputParseTableAtermFile, inputParseTablePersistedFile, outputParseTableAtermFile, outputParseTablePersistedFile) -> KeyedMessages.of())
            ;
    }
}
