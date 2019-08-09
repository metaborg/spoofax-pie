package mb.spoofax.core.language.command;

import mb.common.util.EnumSetView;
import mb.pie.api.Task;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;

import java.io.Serializable;

public interface CommandDef<A extends Serializable> {
    String getId();

    String getDisplayName();


    EnumSetView<CommandExecutionType> getSupportedExecutionTypes();

    EnumSetView<CommandContextType> getRequiredContextTypes();


    ParamDef getParamDef();

    A fromRawArgs(RawArgs rawArgs);


    Task<CommandOutput> createTask(CommandInput<A> input);
}
