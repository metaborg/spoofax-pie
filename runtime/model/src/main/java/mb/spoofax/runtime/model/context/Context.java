package mb.spoofax.runtime.model.context;

import java.io.Serializable;

import mb.vfs.path.PPath;

public interface Context extends Serializable {
    PPath currentDir();
}
