package mb.vfs.path;

import mb.vfs.list.*;

import java.util.Collection;

public class PPaths {
    public static PathMatcher allPathMatcher() {
        return new AllPathMatcher();
    }

    public static PathWalker allPathWalker() {
        return new AllPathWalker();
    }


    public static PathMatcher extensionsPathMatcher(Collection<String> extensions) {
        return new ExtensionsPathMatcher(extensions);
    }

    public static PathWalker extensionsPathWalker(Collection<String> extensions) {
        return new ExtensionsPathWalker(extensions);
    }


    public static PathMatcher directoryPathMatcher() {
        // TODO: make ignoring hidden directories configurable
        return new DirectoryPathMatcher(true);
    }

    public static PathWalker directoryPathWalker() {
        // TODO: make ignoring hidden directories configurable
        return new DirectoryPathWalker(true);
    }
}
