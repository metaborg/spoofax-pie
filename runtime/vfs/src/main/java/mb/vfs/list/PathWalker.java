package mb.vfs.list;

import mb.vfs.path.PPath;

public interface PathWalker extends PathMatcher {
    boolean traverse(PPath path, PPath root);

    @Override boolean matches(PPath path, PPath root);
}
