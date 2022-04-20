package mb.sdf3.task.util;

import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

public class Sdf3Util {
    public static ResourceWalker createResourceWalker() {
        return ResourceWalker
            .ofNoHidden()
            .and(ResourceWalker.ofNot(ResourceWalker.ofPath(PathMatcher.ofStartsWith("bin")))); // HACK: ignore bin directory in root directory
    }

    public static ResourceMatcher createResourceMatcher() {
        return ResourceMatcher.ofPath(PathMatcher.ofExtensions("sdf3", "tmpl")).and(ResourceMatcher.ofFile());
    }
}
