package mb.pipe.run.core.model;

import mb.vfs.path.PPath;

public interface ContextFactory {
    Context create(PPath currentDir);
}
