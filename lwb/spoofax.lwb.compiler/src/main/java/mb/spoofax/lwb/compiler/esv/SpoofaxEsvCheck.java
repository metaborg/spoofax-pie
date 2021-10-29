package mb.spoofax.lwb.compiler.esv;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ListView;
import mb.esv.task.EsvCheck;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;

/**
 * Check task for ESV in the context of the Spoofax LWB compiler.
 */
public class SpoofaxEsvCheck implements TaskDef<ResourcePath, KeyedMessages> {
    private final SpoofaxEsvConfigure configure;
    private final EsvCheck check;

    @Inject public SpoofaxEsvCheck(
        SpoofaxEsvConfigure configure,
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
                c -> check(context, c),
                KeyedMessages::of
            ),
            e -> KeyedMessages.ofTryExtractMessagesFrom(e, rootDirectory)
                .orElse(KeyedMessages.of(ListView.of(new Message("Could not check ESV because it could not be configured", e)), rootDirectory))
        );
    }

    public KeyedMessages check(ExecContext context, SpoofaxEsvConfig config) {
        return config.caseOf()
            .files((esvConfig, outputFile) -> context.require(check, esvConfig))
            .prebuilt((inputFile, outputFile) -> KeyedMessages.of())
            ;
    }
}
