package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CommandAction implements MenuItem {
    private final CommandRequest commandRequest;
    private final @Nullable String displayName;

    public CommandAction(CommandRequest commandRequest, @Nullable String displayName) {
        this.commandRequest = commandRequest;
        this.displayName = displayName;
    }

    public CommandAction(CommandRequest commandRequest) {
        this(commandRequest, null);
    }

    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType, String displayName, RawArgs initialArgs) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType, initialArgs), displayName);
    }

    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType, String displayName) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType), displayName);
    }

    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType, RawArgs initialArgs) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType, initialArgs), commandDef.getDisplayName());
    }

    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType), commandDef.getDisplayName());
    }

    public static CommandAction ofManualOnce(CommandDef<?> commandDef, String suffix, RawArgs initialArgs) {
        return of(commandDef, CommandExecutionType.ManualOnce, commandDef.getDisplayName() + " " + suffix, initialArgs);
    }

    public static CommandAction ofManualOnce(CommandDef<?> commandDef, String suffix) {
        return of(commandDef, CommandExecutionType.ManualOnce, commandDef.getDisplayName() + " " + suffix);
    }

    public static CommandAction ofManualOnce(CommandDef<?> commandDef) {
        return of(commandDef, CommandExecutionType.ManualOnce);
    }

    public static CommandAction ofManualContinuous(CommandDef<?> commandDef, String suffix, RawArgs initialArgs) {
        return of(commandDef, CommandExecutionType.ManualContinuous, commandDef.getDisplayName() + " " + suffix + " (continuous)", initialArgs);
    }

    public static CommandAction ofManualContinuous(CommandDef<?> commandDef, String suffix) {
        return of(commandDef, CommandExecutionType.ManualContinuous, commandDef.getDisplayName() + " " + suffix + " (continuous)");
    }

    public static CommandAction ofManualContinuous(CommandDef<?> commandDef) {
        return of(commandDef, CommandExecutionType.ManualContinuous, commandDef.getDisplayName() + " (continuous)");
    }


    public CommandRequest getCommandRequest() {
        return commandRequest;
    }

    @Override public String getDisplayName() {
        if(displayName != null) {
            return displayName;
        } else {
            return commandRequest.def.getDisplayName();
        }
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.command(getDisplayName(), commandRequest);
    }
}
