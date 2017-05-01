package mb.pipe.run.eclipse.vfs;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;

import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.core.vfs.IResourceSrv;
import mb.pipe.run.eclipse.util.Nullable;

/**
 * Extension of the resource service with Eclipse-specific functionality.
 */
public interface IEclipseResourceSrv extends IResourceSrv {
    /**
     * Converts an Eclipse resource into a VFS resource.
     * 
     * @param resource
     *            Eclipse resource to convert.
     * @return VFS resource.
     */
    IResource resolve(org.eclipse.core.resources.IResource resource);

    /**
     * Converts an Eclipse path into a VFS resource.
     * 
     * @param path
     *            Path to convert.
     * @return VFS resource.
     */
    IResource resolve(IPath path);

    /**
     * Converts the Eclipse workspace root into a VFS resource.
     * 
     * @return VFS resource.
     */
    IResource resolveWorkspaceRoot();

    /**
     * Converts an Eclipse editor input into a VFS resource, if possible.
     * 
     * @param input
     *            Eclipse editor input to resolve.
     * @return VFS resource, or null if it could not be converted.
     */
    @Nullable IResource resolve(IEditorInput input);

    /**
     * Converts a VFS resource into an Eclipse resource, if possible
     * 
     * @param resource
     *            VFS resource
     * @return Eclipse resource, or null if it could not be converted.
     */
    @Nullable org.eclipse.core.resources.IResource unresolve(IResource resource);
}
