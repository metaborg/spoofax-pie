package mb.statix.util;

import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

public class StatixUtil {
    public static ResourceWalker createResourceWalker() {
        return ResourceWalker.ofPath(PathMatcher.ofNoHidden());
    }

    public static ResourceMatcher createResourceMatcher() {
        return ResourceMatcher.ofPath(PathMatcher.ofExtensions("stx")).and(ResourceMatcher.ofFile());
    }
}
