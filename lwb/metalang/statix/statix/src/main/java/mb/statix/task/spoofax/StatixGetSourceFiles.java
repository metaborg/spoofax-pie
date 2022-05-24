package mb.statix.task.spoofax;

import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.task.StatixConfig;
import mb.statix.util.StatixUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@StatixScope
public class StatixGetSourceFiles implements TaskDef<ResourcePath, ListView<ResourcePath>> {
    private final Logger logger;
    private final StatixClassLoaderResources classLoaderResources;
    private final StatixConfigFunctionWrapper configFunctionWrapper;
    private final BaseStatixGetSourceFiles baseGetSourceFiles;

    @Inject
    public StatixGetSourceFiles(
        LoggerFactory loggerFactory,
        StatixClassLoaderResources classLoaderResources,
        StatixConfigFunctionWrapper configFunctionWrapper,
        BaseStatixGetSourceFiles baseGetSourceFiles
    ) {
        this.logger = loggerFactory.create(getClass());
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
            o -> o.mapThrowingOrElse(c -> getSourceFiles(context, c), () -> context.require(baseGetSourceFiles, rootDirectory)),
            e -> {
                logger.warn("Getting Statix source files according to configuration failed, reading configuration failed unexpectedly; falling back to {} files", e, StatixUtil.fileExtensions);
                return context.require(baseGetSourceFiles, rootDirectory);
            }
        );
    }

    private ListView<ResourcePath> getSourceFiles(ExecContext context, StatixConfig config) throws IOException {
        final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
        config.sourceFileOrigins.forEach(context::require);
        final ResourcePath mainFile = config.mainFile;
        sourceFiles.add(mainFile);
        for(ResourcePath directory : config.sourceAndIncludePaths()) {
            addSourceFiles(context, directory, mainFile, sourceFiles);
        }
        return ListView.of(sourceFiles);
    }

    private void addSourceFiles(
        ExecContext context,
        ResourcePath sourceDirectory,
        ResourcePath mainFile,
        ArrayList<ResourcePath> sourceFiles
    ) throws IOException {
        final HierarchicalResource directory = context.require(sourceDirectory);
        if(!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Statix source directory '" + directory + "' does not exist or is not a directory");
        }
        // NOTE: Require directories recursively, so we re-execute whenever a file is added/removed from
        // a directory. Also require each directory separately, to ensure that changes in directories
        // are properly detected in bottom-up builds.
        directory.walkForEach(StatixUtil.resourceWalker, StatixUtil.directoryMatcher, context::require);
        directory.walkForEach(StatixUtil.resourceWalker, StatixUtil.fileMatcher, file -> {
            final ResourcePath path = file.getPath();
            if(!path.equals(mainFile)) {
                sourceFiles.add(path);
            }
        });
    }
}
