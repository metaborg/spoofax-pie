package mb.str.util;

import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

public class StrategoUtil {
    public static ResourceWalker createResourceWalker() {
        return ResourceWalker.ofPath(PathMatcher.ofNoHidden());
    }

    public static ResourceMatcher createResourceMatcher() {
        return ResourceMatcher.ofPath(PathMatcher.ofExtensions("str", "str2")).and(ResourceMatcher.ofFile());
    }
}
