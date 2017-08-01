package mb.vfs.list;

import mb.vfs.path.PPath;

import java.io.IOException;
import java.util.stream.Stream;

public interface PathWalker extends PathMatcher {
    boolean traverse(PPath path, PPath root);

    @Override boolean matches(PPath path, PPath root);


    default Stream<PPath> walk(PPath path) throws IOException {
        return path.walk(this);
    }
}
