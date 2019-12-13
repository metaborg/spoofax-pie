package mb.spoofax.core.language.command;

import mb.pie.api.Task;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class AutoCommandRequest<A extends Serializable> {
    public final CommandDef<A> def;
    public final @Nullable RawArgs initialArgs;

    public AutoCommandRequest(CommandDef<A> def, @Nullable RawArgs initialArgs) {
        this.def = def;
        this.initialArgs = initialArgs;
    }

    public AutoCommandRequest(CommandDef<A> def) {
        this(def, null);
    }

    public CommandRequest<A> toCommandRequest() {
        return new CommandRequest<>(this.def, CommandExecutionType.AutomaticContinuous, initialArgs);
    }

    public Task<CommandOutput> createTask(CommandContext context, ArgConverters argConverters) {
        return def.createTask(toCommandRequest(), context, argConverters);
    }
}
