package mb.sdf3.task.util;

import mb.common.util.ListView;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.match.path.string.ExtensionsPathStringMatcher;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

public class Sdf3Util {
    public static final String displayName = "SDF3";

    public static final ListView<String> fileExtensions = ListView.of("sdf3", "tmpl");
    public static final String[] fileExtensionsArray = fileExtensions.toArray(new String[0]);
    public static final ExtensionsPathMatcher extensionsPathMatcher = PathMatcher.ofExtensions(fileExtensionsArray);
    public static final ExtensionsPathStringMatcher extensionsPathStringMatcher = PathStringMatcher.ofExtensions(fileExtensionsArray);

    public static final ResourceMatcher fileMatcher = ResourceMatcher
        .ofPath(extensionsPathMatcher)
        .and(ResourceMatcher.ofFile());
    public static final ResourceMatcher directoryMatcher = ResourceMatcher.ofDirectory();
    public static final ResourceWalker resourceWalker = ResourceWalker
        .ofNoHidden()
        .and(ResourceWalker.ofNot(ResourceWalker.ofPath(PathMatcher.ofStartsWith("bin")))); // HACK: ignore bin directory in root directory
}
