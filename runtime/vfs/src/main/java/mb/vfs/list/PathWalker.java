package mb.vfs.list;

import mb.vfs.path.PPath;

public interface PathWalker extends PathMatcher {
    boolean traverse(PPath path);

    boolean matches(PPath path);
}
