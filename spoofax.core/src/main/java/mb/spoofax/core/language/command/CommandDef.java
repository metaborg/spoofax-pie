package mb.spoofax.core.language.command;

import mb.common.util.EnumSetView;
import mb.pie.api.Task;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.command.arg.RawArgsBuilder;

import java.io.Serializable;

public interface CommandDef<A extends Serializable> {
    String getId();

    String getDisplayName();


    EnumSetView<CommandExecutionType> getSupportedExecutionTypes();

    EnumSetView<CommandContextType> getRequiredContextTypes();


    ParamDef getParamDef();

    A fromRawArgs(RawArgs rawArgs);


    Task<CommandOutput> createTask(A args);


    default CommandRequest<A> request(CommandExecutionType executionType, RawArgs initialArgs) {
        return new CommandRequest<>(this, executionType, initialArgs);
    }

    default CommandRequest<A> request(CommandExecutionType executionType) {
        return new CommandRequest<>(this, executionType);
    }

    default Task<CommandOutput> createTask(CommandRequest<A> request, CommandContext context, ArgConverters argConverters) {
        final RawArgsBuilder builder = new RawArgsBuilder(getParamDef(), argConverters);
        if(request.initialArgs != null) {
            builder.setArgsFrom(request.initialArgs);
        }
        final RawArgs rawArgs = builder.build(context);
        final A args = fromRawArgs(rawArgs);
        return createTask(args);
    }
}
