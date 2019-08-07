package mb.spoofax.core.language.menu;

import mb.spoofax.core.language.command.CommandRequest;
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
