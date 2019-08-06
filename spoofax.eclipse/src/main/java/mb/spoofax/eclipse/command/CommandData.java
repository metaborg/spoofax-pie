package mb.spoofax.eclipse.command;

import mb.common.util.ListView;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandExecutionType;

import java.io.Serializable;
import java.util.Objects;

public class CommandData implements Serializable {
    public final String commandId;
    public final CommandExecutionType executionType;
    public final ListView<CommandContext> contexts;

    public CommandData(String commandId, CommandExecutionType executionType, ListView<CommandContext> contexts) {
        this.commandId = commandId;
        this.executionType = executionType;
        this.contexts = contexts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CommandData that = (CommandData) o;
        return commandId.equals(that.commandId) &&
            executionType == that.executionType &&
            contexts.equals(that.contexts);
    }

    @Override public int hashCode() {
        return Objects.hash(commandId, executionType, contexts);
    }

    @Override public String toString() {
        return "TransformData(" +
            "commandId='" + commandId + '\'' +
            ", executionType=" + executionType +
            ", contexts=" + contexts +
            ')';
    }
}
