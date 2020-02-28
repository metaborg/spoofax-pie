package mb.spoofax.eclipse.command;

import mb.common.util.ListView;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class CommandData implements Serializable {
    public final String commandId;
    public final CommandExecutionType executionType;
    public final @Nullable RawArgs initialArgs;
    public final ListView<? extends CommandContext> contexts;

    public CommandData(String commandId, CommandExecutionType executionType, @Nullable RawArgs initialArgs, ListView<? extends CommandContext> contexts) {
        this.commandId = commandId;
        this.executionType = executionType;
        this.initialArgs = initialArgs;
        this.contexts = contexts;
    }

    public CommandData(CommandRequest<?> commandRequest, ListView<? extends CommandContext> contexts) {
        this(commandRequest.def().getId(), commandRequest.executionType(), commandRequest.initialArgs().orElse(null), contexts);
    }

    public CommandRequest<?> toCommandRequest(CommandDef<?> def) {
        return CommandRequest.of(def, executionType, initialArgs);
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CommandData other = (CommandData)obj;
        return commandId.equals(other.commandId) &&
            executionType == other.executionType &&
            Objects.equals(initialArgs, other.initialArgs) &&
            contexts.equals(other.contexts);
    }

    @Override public int hashCode() {
        return Objects.hash(commandId, executionType, initialArgs, contexts);
    }

    @Override public String toString() {
        return "CommandData(" +
            "commandId='" + commandId + '\'' +
            ", executionType=" + executionType +
            ", args=" + initialArgs +
            ", contexts=" + contexts +
            ')';
    }
}
