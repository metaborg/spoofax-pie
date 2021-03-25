package mb.sdf3.task.util;

import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

public class Sdf3Util {
    public static ResourceWalker createResourceWalker() {
        return ResourceWalker.ofPath(PathMatcher.ofNoHidden());
    }

    public static ResourceMatcher createResourceMatcher() {
        return ResourceMatcher.ofPath(PathMatcher.ofExtensions("tmpl", "sdf3")).and(ResourceMatcher.ofFile());
    }
}
