package mb.pipe.run.core.path;

import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;

public interface PPath extends Serializable {
    URI getUri();

    Path getJavaPath();

    PPath resolve(String other);
}
