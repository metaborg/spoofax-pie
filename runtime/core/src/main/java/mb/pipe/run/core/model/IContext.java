package mb.pipe.run.core.model;

import java.io.Serializable;

import mb.pipe.run.core.vfs.IResource;

public interface IContext extends Serializable {
    IResource currentDir();
    
    IResource persistentDir();
}
