package mb.spoofax.runtime.model.context;

import mb.vfs.path.PPath;

public interface ContextFactory {
    Context create(PPath currentDir);
}
