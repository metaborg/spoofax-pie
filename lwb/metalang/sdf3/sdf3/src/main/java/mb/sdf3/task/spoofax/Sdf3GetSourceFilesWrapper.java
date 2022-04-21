package mb.sdf3.task.spoofax;

import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.util.Sdf3Util;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@Sdf3Scope
public class Sdf3GetSourceFilesWrapper implements TaskDef<ResourcePath, ListView<ResourcePath>> {
    private final Logger logger;
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3SpecConfigFunctionWrapper configFunctionWrapper;
    private final BaseSdf3GetSourceFiles baseGetSourceFiles;

    @Inject
    public Sdf3GetSourceFilesWrapper(
        LoggerFactory loggerFactory,
        Sdf3ClassLoaderResources classLoaderResources,
        Sdf3SpecConfigFunctionWrapper configFunctionWrapper,
        BaseSdf3GetSourceFiles baseGetSourceFiles
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
                logger.warn("Getting SDF3 source files according to configuration failed, reading configuration failed unexpectedly; falling back to {} files", e, Sdf3Util.fileExtensions);
                return context.require(baseGetSourceFiles, rootDirectory);
            }
        );
    }

    private ListView<ResourcePath> getSourceFiles(ExecContext context, Sdf3SpecConfig config) throws IOException {
        final ArrayList<ResourcePath> sourceFiles = new ArrayList<>();
        config.sourceFileOrigins.forEach(context::require);
        final ResourcePath mainFile = config.mainFile;
        sourceFiles.add(mainFile);
        addSourceFiles(context, config.mainSourceDirectory, mainFile, sourceFiles);
        for(ResourcePath includeDirectory : config.includeDirectories) {
            addSourceFiles(context, includeDirectory, mainFile, sourceFiles);
        }
        return ListView.of(sourceFiles);
    }

    private void addSourceFiles(
        ExecContext context,
        ResourcePath sourceDirectory,
        ResourcePath mainFile,
        ArrayList<ResourcePath> sourceFiles
    ) throws IOException {
        final HierarchicalResource directory = context.getHierarchicalResource(sourceDirectory);
        if(!directory.exists() || !directory.isDirectory()) {
            throw new IOException("SDF3 source directory '" + directory + "' does not exist or is not a directory");
        }
        // NOTE: Require directories recursively, so we re-execute whenever a file is added/removed from
        // a directory. Also require each directory separately, to ensure that changes in directories
        // are properly detected in bottom-up builds.
        directory.walkForEach(Sdf3Util.resourceWalker, Sdf3Util.directoryMatcher, context::require);
        directory.walkForEach(Sdf3Util.resourceWalker, Sdf3Util.fileMatcher, file -> {
            final ResourcePath path = file.getPath();
            if(!path.equals(mainFile)) {
                sourceFiles.add(path);
            }
        });
    }
}
