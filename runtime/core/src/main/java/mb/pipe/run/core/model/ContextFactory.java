package mb.pipe.run.core.model;

import mb.pipe.run.core.path.PPath;

public interface ContextFactory {
    Context create(PPath currentDir);
}
