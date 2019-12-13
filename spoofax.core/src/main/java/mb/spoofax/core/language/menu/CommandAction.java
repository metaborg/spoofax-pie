package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A menu command item.
 */
@SuppressWarnings("rawtypes")
public final class CommandAction implements MenuItem {

    private final CommandRequest commandRequest;
    private final @Nullable String displayName;
    private final @Nullable String description;

    /**
     * Initializes a new instance of the {@link CommandAction} class.
     *
     * @param commandRequest The command request.
     * @param displayName    The display name; or null to use the command definition's display name.
     * @param description    The short description of the command; or null to use the command definition's description.
     */
    public CommandAction(CommandRequest commandRequest, @Nullable String displayName, @Nullable String description) {
        this.commandRequest = commandRequest;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Initializes a new instance of the {@link CommandAction} class.
     *
     * @param commandRequest The command request.
     * @param displayName    The display name; or null to use the command request's default display name.
     */
    public CommandAction(CommandRequest commandRequest, @Nullable String displayName) {
        this(commandRequest, displayName, null);
    }

    /**
     * Initializes a new instance of the {@link CommandAction} class.
     *
     * @param commandRequest The command request.
     */
    public CommandAction(CommandRequest commandRequest) {
        this(commandRequest, null);
    }

    /**
     * Creates a command action of the specified command.
     *
     * @param commandDef the command definition
     * @param executionType the execution type, which is one of the {@link CommandExecutionType} members
     * @param displayName the display name
     * @param initialArgs the initial arguments to the command
     * @return the created command action
     */
    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType, String displayName, RawArgs initialArgs) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType, initialArgs), displayName);
    }

    /**
     * Creates a command action of the specified command, supplying no initial arguments.
     *
     * @param commandDef the command definition
     * @param executionType the execution type, which is one of the {@link CommandExecutionType} members
     * @param displayName the display name
     * @return the created command action
     */
    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType, String displayName) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType), displayName);
    }

    /**
     * Creates a command action of the specified command, using the display name of the command definition.
     *
     * @param commandDef the command definition
     * @param executionType the execution type, which is one of the {@link CommandExecutionType} members
     * @param initialArgs the initial arguments to the command
     * @return the created command action
     */
    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType, RawArgs initialArgs) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType, initialArgs), commandDef.getDisplayName());
    }

    /**
     * Creates a command action of the specified command, using the display name of the command definition
     * and supplying no initial arguments.
     *
     * @param commandDef the command definition
     * @param executionType the execution type, which is one of the {@link CommandExecutionType} members
     * @return the created command action
     */
    public static CommandAction of(CommandDef<?> commandDef, CommandExecutionType executionType) {
        return new CommandAction(new CommandRequest<>(commandDef, executionType), commandDef.getDisplayName());
    }

    /**
     * Creates an execute-once manual command action of the specified command,
     * using the display name of the command definition with the specified suffix.
     *
     * @param commandDef the command definition
     * @param suffix the suffix to add to the command definition's display name
     * @param initialArgs the initial arguments to the command
     * @return the created command action
     */
    public static CommandAction ofManualOnce(CommandDef<?> commandDef, String suffix, RawArgs initialArgs) {
        return of(commandDef, CommandExecutionType.ManualOnce, commandDef.getDisplayName() + " " + suffix, initialArgs);
    }

    /**
     * Creates an execute-once manual command action of the specified command,
     * using the display name of the command definition with the specified suffix, supplying no initial arguments.
     *
     * @param commandDef the command definition
     * @param suffix the suffix to add to the command definition's display name
     * @return the created command action
     */
    public static CommandAction ofManualOnce(CommandDef<?> commandDef, String suffix) {
        return of(commandDef, CommandExecutionType.ManualOnce, commandDef.getDisplayName() + " " + suffix);
    }

    /**
     * Creates an execute-once manual command action of the specified command,
     * using the display name of the command definition, supplying no initial arguments.
     *
     * @param commandDef the command definition
     * @return the created command action
     */
    public static CommandAction ofManualOnce(CommandDef<?> commandDef) {
        return of(commandDef, CommandExecutionType.ManualOnce);
    }

    /**
     * Creates an execute-continuous manual command action of the specified command,
     * using the display name of the command definition with the specified suffix.
     *
     * @param commandDef the command definition
     * @param suffix the suffix to add to the command definition's display name
     * @param initialArgs the initial arguments to the command
     * @return the created command action
     */
    public static CommandAction ofManualContinuous(CommandDef<?> commandDef, String suffix, RawArgs initialArgs) {
        return of(commandDef, CommandExecutionType.ManualContinuous, commandDef.getDisplayName() + " " + suffix + " (continuous)", initialArgs);
    }

    /**
     * Creates an execute-continuous manual command action of the specified command,
     * using the display name of the command definition with the specified suffix, supplying no initial arguments.
     *
     * @param commandDef the command definition
     * @param suffix the suffix to add to the command definition's display name
     * @return the created command action
     */
    public static CommandAction ofManualContinuous(CommandDef<?> commandDef, String suffix) {
        return of(commandDef, CommandExecutionType.ManualContinuous, commandDef.getDisplayName() + " " + suffix + " (continuous)");
    }

    /**
     * Creates an execute-continuous manual command action of the specified command,
     * using the display name of the command definition, supplying no initial arguments.
     *
     * @param commandDef the command definition
     * @return the created command action
     */
    public static CommandAction ofManualContinuous(CommandDef<?> commandDef) {
        return of(commandDef, CommandExecutionType.ManualContinuous, commandDef.getDisplayName() + " (continuous)");
    }

    /**
     * Gets the command request.
     *
     * @return The command request.
     */
    public CommandRequest getCommandRequest() {
        return this.commandRequest;
    }

    @Override public String getDisplayName() {
        if(displayName != null) {
            return displayName;
        } else {
            return commandRequest.def.getDisplayName();
        }
    }

    @Override public String getDescription() {
        if(description != null) {
            return description;
        } else {
            return commandRequest.def.getDescription();
        }
    }

    @Override public void accept(MenuItemVisitor visitor) {
        visitor.command(this);
    }

}
