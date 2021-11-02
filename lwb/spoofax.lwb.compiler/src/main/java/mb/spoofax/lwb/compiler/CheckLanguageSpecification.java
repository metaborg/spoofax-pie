package mb.spoofax.lwb.compiler;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.cfg.SpoofaxCfgCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCheck;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Check;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCheck;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoCheck;
import org.immutables.value.Value;

import javax.inject.Inject;

/**
 * Checks a language specification by running the meta-language checkers.
 *
 * Takes as input a {@link ResourcePath} path to the root directory of the language specification.
 *
 * Returns all {@link KeyedMessages messages} produced by the meta-language checkers.
 */
@Value.Enclosing
public class CheckLanguageSpecification implements TaskDef<ResourcePath, KeyedMessages> {
    private final SpoofaxCfgCheck spoofaxCfgCheck;
    private final SpoofaxSdf3Check spoofaxSdf3Check;
    private final SpoofaxEsvCheck spoofaxEsvCheck;
    private final SpoofaxStatixCheck spoofaxStatixCheck;
    private final SpoofaxStrategoCheck spoofaxStrategoCheck;

    @Inject public CheckLanguageSpecification(
        SpoofaxCfgCheck spoofaxCfgCheck,
        SpoofaxSdf3Check spoofaxSdf3Check,
        SpoofaxEsvCheck spoofaxEsvCheck,
        SpoofaxStatixCheck spoofaxStatixCheck,
        SpoofaxStrategoCheck spoofaxStrategoCheck
    ) {
        this.spoofaxCfgCheck = spoofaxCfgCheck;
        this.spoofaxSdf3Check = spoofaxSdf3Check;
        this.spoofaxEsvCheck = spoofaxEsvCheck;
        this.spoofaxStatixCheck = spoofaxStatixCheck;
        this.spoofaxStrategoCheck = spoofaxStrategoCheck;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath rootDirectory) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        messagesBuilder.addMessages(context.require(spoofaxCfgCheck, rootDirectory));
        messagesBuilder.addMessages(context.require(spoofaxSdf3Check, rootDirectory));
        messagesBuilder.addMessages(context.require(spoofaxEsvCheck, rootDirectory));
        messagesBuilder.addMessages(context.require(spoofaxStatixCheck, rootDirectory));
        messagesBuilder.addMessages(context.require(spoofaxStrategoCheck, rootDirectory));
        return messagesBuilder.build();
    }
}
