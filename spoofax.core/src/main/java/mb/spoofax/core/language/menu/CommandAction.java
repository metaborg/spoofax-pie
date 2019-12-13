package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.command.CommandRequest;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * A menu command item.
 */
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
