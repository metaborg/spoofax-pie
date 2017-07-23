package mb.pipe.run.core.path;

import java.io.Serializable;

@FunctionalInterface
public interface PathMatcher extends Serializable {
    boolean matches(PPath path);
}
