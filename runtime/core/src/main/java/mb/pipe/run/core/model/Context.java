package mb.pipe.run.core.model;

import java.io.Serializable;

import mb.pipe.run.core.path.PPath;

public interface Context extends Serializable {
    PPath currentDir();
    
    PPath persistentDir();
}
