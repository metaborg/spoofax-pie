package mb.dynamix.task.spoofax;

import mb.common.util.ListView;
import mb.dynamix.DynamixClassLoaderResources;
import mb.dynamix.DynamixScope;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@DynamixScope
public class DynamixGetSourceFiles implements TaskDef<ResourcePath, ListView<ResourcePath>> {
    private final DynamixClassLoaderResources classLoaderResources;
    private final DynamixConfigFunctionWrapper configFunctionWrapper;
    private final BaseDynamixGetSourceFiles baseGetSourceFiles;

    @Inject
    public DynamixGetSourceFiles(
        DynamixClassLoaderResources classLoaderResources,
        DynamixConfigFunctionWrapper configFunctionWrapper,
        BaseDynamixGetSourceFiles baseGetSourceFiles
    ) {
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
        this.baseGetSourceFiles = baseGetSourceFiles;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public ListView<ResourcePath> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        return configFunctionWrapper.get().apply(context, rootDirectory).mapThrowingOrElse(
            o -> o.mapThrowingOrElse(
                config -> {
                    final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
                    final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());

                    // All .dx files in source and include directories.
                    final ResourceMatcher dxMatcher = ResourceMatcher.ofPath(PathMatcher.ofExtension("dx")).and(ResourceMatcher.ofFile());
                    for(ResourcePath path : config.sourceAndIncludePaths()) {
                        context
                            .require(path, ResourceStampers.modifiedDirRec(walker, dxMatcher))
                            .walkForEach(walker, dxMatcher, f -> sourceFiles.add(f.getPath()));
                    }

                    return ListView.of(sourceFiles);
                },
                () -> context.require(baseGetSourceFiles, rootDirectory)
            ),
            e -> context.require(baseGetSourceFiles, rootDirectory)
        );
    }
}
