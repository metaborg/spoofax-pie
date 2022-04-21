package mb.str.util;

import mb.common.util.ListView;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.match.path.string.ExtensionsPathStringMatcher;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

public class StrategoUtil {
    public static final String displayName = "Stratego";

    public static final ListView<String> fileExtensions = ListView.of("str", "str2");
    public static final String[] fileExtensionsArray = fileExtensions.toArray(new String[0]);
    public static final ExtensionsPathMatcher extensionsPathMatcher = PathMatcher.ofExtensions(fileExtensionsArray);
    public static final ExtensionsPathStringMatcher extensionsPathStringMatcher = PathStringMatcher.ofExtensions(fileExtensionsArray);

    public static final ResourceMatcher resourceMatcher = ResourceMatcher
        .ofPath(extensionsPathMatcher)
        .and(ResourceMatcher.ofFile());
    public static final ResourceWalker resourceWalker = ResourceWalker
        .ofNoHidden()
        .and(ResourceWalker.ofNot(ResourceWalker.ofPath(PathMatcher.ofStartsWith("bin")))); // HACK: ignore bin directory in root directory
}
