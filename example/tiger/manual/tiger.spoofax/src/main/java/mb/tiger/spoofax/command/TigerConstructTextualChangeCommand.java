package mb.tiger.spoofax.command;

import mb.common.util.EnumSetView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.task.TigerShowReconstructedAst;

import javax.inject.Inject;

public class TigerConstructTextualChangeCommand implements CommandDef<ResourceKey>  {

    private final TigerShowReconstructedAst showReconstructedAst;

    @Inject
    public TigerConstructTextualChangeCommand(TigerShowReconstructedAst showReconstructedAst) {
        this.showReconstructedAst = showReconstructedAst;
    }

    @Override
    public String getId() {
        return TigerConstructTextualChangeCommand.class.getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return TigerConstructTextualChangeCommand.class.getSimpleName();
    }

    @Override
    public String getDescription() {
        return TigerConstructTextualChangeCommand.class.getSimpleName();
    }

    @Override
    public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce);
    }

    @Override
    public ParamDef getParamDef() {
        return new ParamDef(Param.of("resource", ResourceKey.class));
    }

    @Override
    public ResourceKey fromRawArgs(RawArgs rawArgs) {
        return rawArgs.getOrThrow("resource");
    }

    @Override
    public Task<CommandFeedback> createTask(ResourceKey file) {        ;
        return showReconstructedAst.createTask(file);
    }
}
