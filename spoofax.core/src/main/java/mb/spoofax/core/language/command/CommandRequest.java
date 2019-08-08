package mb.spoofax.core.language.command;

import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class CommandRequest<A extends Serializable> {
    public final CommandDef<A> def;
    public final CommandExecutionType executionType;
    public final @Nullable RawArgs initialArgs;

    public CommandRequest(CommandDef<A> def, CommandExecutionType executionType, @Nullable RawArgs initialArgs) {
        this.def = def;
        this.executionType = executionType;
        this.initialArgs = initialArgs;
    }

    public CommandRequest(CommandDef<A> def, CommandExecutionType executionType) {
        this(def, executionType, null);
    }
}
