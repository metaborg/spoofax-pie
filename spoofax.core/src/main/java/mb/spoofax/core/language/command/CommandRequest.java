package mb.spoofax.core.language.command;

public class CommandRequest {
    public final CommandDef<?> def;
    public final CommandExecutionType executionType;


    public CommandRequest(CommandDef<?> def, CommandExecutionType executionType) {
        this.def = def;
        this.executionType = executionType;
    }
}
