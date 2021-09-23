package mb.tiger.spoofax.task;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TigerScope
public class TigerGetSourceFiles implements TaskDef<ResourcePath, ListView<ResourcePath>> {
    private static final ResourceWalker WALKER = ResourceWalker.ofNoHidden();
    private static final ResourceMatcher MATCHER = ResourceMatcher.ofPath(PathMatcher.ofExtensions("tig")).and(ResourceMatcher.ofFile());

    @Inject
    public TigerGetSourceFiles() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public ListView<ResourcePath> exec(ExecContext context, ResourcePath input) throws Exception {
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(WALKER, ResourceMatcher.ofDirectory(), context::require);
        try(final Stream<? extends HierarchicalResource> files = rootDirectory.walk(WALKER, MATCHER)) {
            return ListView.of(files.map(HierarchicalResource::getPath).collect(Collectors.toList()));
        }
    }
}
