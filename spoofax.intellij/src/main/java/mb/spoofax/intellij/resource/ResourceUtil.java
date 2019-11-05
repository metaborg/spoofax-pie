package mb.spoofax.intellij.resource;

import com.intellij.openapi.vfs.VirtualFile;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.fs.FSResource;
import mb.resource.text.TextResource;
import mb.resource.url.URLResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.Path;


/**
 * Utility functions for working with resources.
 */
@Singleton
public final class ResourceUtil {
    private final ResourceService resourceService;

    @Inject
    public ResourceUtil(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * Gets the virtual file corresponding to the specified resource key.
     *
     * @param key The resource key.
     * @return The corresponding virtual file.
     */
    public VirtualFile getVirtualFile(ResourceKey key) {
        return getVirtualFile(this.resourceService.getResource(key));
    }

    /**
     * Gets the virtual file corresponding to the specified resource.
     *
     * @param resource The resource.
     * @return The corresponding virtual file.
     */
    public VirtualFile getVirtualFile(Resource resource) {
        if (resource instanceof IntellijResource) {
            return ((IntellijResource)resource).getVirtualFile();
        } else if (resource instanceof FSResource){
            Path path = ((FSResource)resource).getJavaPath();
            // TODO: Open VirtualFile to path
            throw new ResourceRuntimeException("Resource '" + resource + "' is not an IntelliJ resource. Not implemented.");
        } else if (resource instanceof TextResource) {
            // TODO: Create Document and corresponding VirtualFile, if possible?
            throw new ResourceRuntimeException("Resource '" + resource + "' is not an IntelliJ resource. Not implemented.");
        } else if (resource instanceof URLResource) {
            URI uri = ((URLResource)resource).getURI();
            // TODO: Open VirtualFile to URI?
            throw new ResourceRuntimeException("Resource '" + resource + "' is not an IntelliJ resource. Not implemented.");
        } else {
            throw new ResourceRuntimeException("Resource '" + resource + "' is not an IntelliJ resource.");
        }
    }
}
