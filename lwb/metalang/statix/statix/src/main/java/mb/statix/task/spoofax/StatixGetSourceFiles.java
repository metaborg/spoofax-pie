package mb.statix.task.spoofax;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.util.StatixUtil;

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

    @Override public ListView<ResourcePath> exec(ExecContext context, ResourcePath input) throws IOException {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        return configFunctionWrapper.get().apply(context, input).mapThrowingOrElse(
            o -> o.mapThrowingOrElse(
                config -> {
                    final ResourceWalker walker = StatixUtil.createResourceWalker();
                    final ResourceMatcher matcher = StatixUtil.createResourceMatcher();
                    final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
                    for(ResourcePath path : config.sourceAndIncludePaths()) {
                        final HierarchicalResource directory = context.require(path, ResourceStampers.modifiedDirRec(walker, matcher));
                        directory.walkForEach(walker, matcher, f -> {
                            sourceFiles.add(f.getPath());
                        });
                    }
                    return ListView.of(sourceFiles);
                },
                () -> context.require(baseGetSourceFiles, input)
            ),
            e -> context.require(baseGetSourceFiles, input)
        );
    }
}
