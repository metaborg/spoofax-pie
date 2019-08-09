package mb.spoofax.core.language.command;

import java.io.Serializable;

public enum CommandContextType implements Serializable {
    Project,
    Directory,
    File,
    Resource,
    Region,
    Offset
}
