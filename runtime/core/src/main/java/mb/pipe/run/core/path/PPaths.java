package mb.pipe.run.core.path;

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
        return new DirectoryPathMatcher();
    }

    public static PathWalker directoryPathWalker() {
        return new DirectoryPathWalker();
    }
}
