package mb.pipe.run.core.path;

public interface PathWalker extends PathMatcher {
    boolean traverse(PPath path);

    boolean matches(PPath path);
}
