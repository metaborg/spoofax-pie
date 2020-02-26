package mb.spoofax.core.language.command;

import java.io.Serializable;

public enum CommandContextType implements Serializable {
    ProjectPath,
    DirectoryPath,
    FilePath,
    ResourcePath,
    ResourceKey,
    Region,
    Offset
}
