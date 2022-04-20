package mb.sdf3.task.spoofax;

import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.util.Sdf3Util;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

@Sdf3Scope
public class Sdf3GetSourceFilesWrapper implements TaskDef<ResourcePath, ListView<ResourcePath>> {
    private static final ResourceWalker WALKER = Sdf3Util.createResourceWalker();
    private static final ResourceMatcher MATCHER = Sdf3Util.createResourceMatcher();

    private final Logger logger;
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3SpecConfigFunctionWrapper configFunctionWrapper;

    @Inject
    public Sdf3GetSourceFilesWrapper(
        LoggerFactory loggerFactory,
        Sdf3ClassLoaderResources classLoaderResources,
        Sdf3SpecConfigFunctionWrapper configFunctionWrapper
    ) {
        this.logger = loggerFactory.create(getClass());
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public ListView<ResourcePath> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());

        final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
        configFunctionWrapper.get().apply(context, rootDirectory).ifThrowingElse(
            o -> o
                .ifSomeThrowing(c -> addSourceFiles(context, c, sourceFiles))
                .ifNoneThrowing(() -> addSourceFiles(context, rootDirectory, null, sourceFiles)), // SDF3 is not configured; just walk over root directory
            e -> logger.error("Cannot get SDF3 source files; reading configuration failed unexpectedly", e)
        );
        return ListView.of(sourceFiles);
    }

    private void addSourceFiles(ExecContext context, Sdf3SpecConfig config, ArrayList<ResourcePath> sourceFiles) throws IOException {
        final ResourcePath mainFile = config.mainFile;
        sourceFiles.add(mainFile);
        addSourceFiles(context, config.mainSourceDirectory, mainFile, sourceFiles);
        for(ResourcePath includeDirectory : config.includeDirectories) {
            addSourceFiles(context, includeDirectory, mainFile, sourceFiles);
        }
    }

    private void addSourceFiles(
        ExecContext context,
        ResourcePath sourceDirectory,
        @Nullable ResourcePath mainFile,
        ArrayList<ResourcePath> sourceFiles
    ) throws IOException {
        final HierarchicalResource directory = context.getHierarchicalResource(sourceDirectory);
        if(!directory.exists() || !directory.isDirectory()) {
            throw new IOException("SDF3 source directory '" + directory + "' does not exist or is not a directory");
        }
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        directory.walkForEach(WALKER, ResourceMatcher.ofDirectory(), context::require);
        try(final Stream<? extends HierarchicalResource> files = directory.walk(WALKER, MATCHER)) {
            files.forEach(f -> {
                final ResourcePath path = f.getPath();
                if(!path.equals(mainFile)) {
                    sourceFiles.add(path);
                }
            });
        }
    }
}
