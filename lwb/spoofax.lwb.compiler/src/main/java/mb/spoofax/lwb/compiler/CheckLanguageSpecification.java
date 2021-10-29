package mb.spoofax.lwb.compiler;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.cfg.CheckCfg;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCheck;
import mb.spoofax.lwb.compiler.sdf3.CheckSdf3;
import mb.spoofax.lwb.compiler.statix.CheckStatix;
import mb.spoofax.lwb.compiler.stratego.CheckStratego;
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
    private final CheckCfg checkCfg;
    private final CheckSdf3 checkSdf3;
    private final SpoofaxEsvCheck spoofaxEsvCheck;
    private final CheckStatix checkStatix;
    private final CheckStratego checkStratego;

    @Inject public CheckLanguageSpecification(
        CheckCfg checkCfg,
        CheckSdf3 checkSdf3,
        SpoofaxEsvCheck spoofaxEsvCheck,
        CheckStatix checkStatix,
        CheckStratego checkStratego
    ) {
        this.checkCfg = checkCfg;
        this.checkSdf3 = checkSdf3;
        this.spoofaxEsvCheck = spoofaxEsvCheck;
        this.checkStatix = checkStatix;
        this.checkStratego = checkStratego;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath rootDirectory) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        messagesBuilder.addMessages(context.require(checkCfg, rootDirectory));
        messagesBuilder.addMessages(context.require(checkSdf3, rootDirectory));
        messagesBuilder.addMessages(context.require(spoofaxEsvCheck, rootDirectory));
        messagesBuilder.addMessages(context.require(checkStatix, rootDirectory));
        messagesBuilder.addMessages(context.require(checkStratego, rootDirectory));
        return messagesBuilder.build();
    }
}
