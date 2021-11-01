package mb.statix.task.spoofax;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@StatixScope
public class StatixGetSourceFiles implements TaskDef<ResourcePath, ListView<ResourcePath>> {
    private final StatixClassLoaderResources classLoaderResources;
    private final StatixConfigFunctionWrapper configFunctionWrapper;
    private final BaseStatixGetSourceFiles baseGetSourceFiles;

    @Inject
    public StatixGetSourceFiles(
        StatixClassLoaderResources classLoaderResources,
        StatixConfigFunctionWrapper configFunctionWrapper,
        BaseStatixGetSourceFiles baseGetSourceFiles
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

                    // All .stx files in source and include directories.
                    final ResourceMatcher stxMatcher = ResourceMatcher.ofPath(PathMatcher.ofExtension("stx")).and(ResourceMatcher.ofFile());
                    for(ResourcePath path : config.sourceAndIncludePaths()) {
                        context
                            .require(path, ResourceStampers.modifiedDirRec(walker, stxMatcher))
                            .walkForEach(walker, stxMatcher, f -> sourceFiles.add(f.getPath()));
                    }

                    // All .stxtest files in the entire project.
                    final ResourceMatcher stxTestMatcher = ResourceMatcher.ofPath(PathMatcher.ofExtension("stxtest")).and(ResourceMatcher.ofFile());
                    context
                        .require(rootDirectory, ResourceStampers.modifiedDirRec(walker, stxTestMatcher))
                        .walkForEach(walker, stxTestMatcher, f -> sourceFiles.add(f.getPath()));

                    return ListView.of(sourceFiles);
                },
                () -> context.require(baseGetSourceFiles, rootDirectory)
            ),
            e -> context.require(baseGetSourceFiles, rootDirectory)
        );
    }
}
