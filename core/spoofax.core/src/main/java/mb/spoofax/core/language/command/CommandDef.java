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

    String getDescription();

    EnumSetView<CommandExecutionType> getSupportedExecutionTypes();


    ParamDef getParamDef();

    A fromRawArgs(RawArgs rawArgs);


    Task<CommandFeedback> createTask(A args);


    default CommandRequest<A> request(CommandExecutionType executionType, RawArgs initialArgs) {
        return CommandRequest.of(this, executionType, initialArgs);
    }

    default CommandRequest<A> request(CommandExecutionType executionType) {
        return CommandRequest.of(this, executionType);
    }

    default Task<CommandFeedback> createTask(CommandRequest<A> request, CommandContext context, ArgConverters argConverters) {
        final RawArgsBuilder builder = new RawArgsBuilder(getParamDef(), argConverters);
        request.initialArgs().ifPresent(builder::setArgsFrom);
        final RawArgs rawArgs = builder.build(context);
        final A args = fromRawArgs(rawArgs);
        return createTask(args);
    }
}
