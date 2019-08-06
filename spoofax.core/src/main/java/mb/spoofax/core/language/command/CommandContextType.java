package mb.spoofax.core.language.command;

import java.io.Serializable;

public enum CommandContextType implements Serializable {
    Project,
    Directory,
    File,
    FileWithRegion,
    FileWithOffset,
    Editor,
    EditorWithRegion,
    EditorWithOffset,
    None
}
