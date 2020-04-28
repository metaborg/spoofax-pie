package mb.spoofax.core.language.command;

import java.io.Serializable;

public enum EnclosingCommandContextType implements Serializable {
    Project,
    Directory;

    public CommandContextType toCommandContextType() {
        switch(this) {
            case Project:
                return CommandContextType.Project;
            case Directory:
                return CommandContextType.Directory;
            default:
                throw new IllegalArgumentException("Failed to convert EnclosingCommandContextType '" + this + "' to a CommandContextType, unhandled enum variant");
        }
    }
}
