package mb.pipe.run.core.model;

import java.io.Serializable;

import mb.vfs.path.PPath;

public interface Context extends Serializable {
    PPath currentDir();
}
