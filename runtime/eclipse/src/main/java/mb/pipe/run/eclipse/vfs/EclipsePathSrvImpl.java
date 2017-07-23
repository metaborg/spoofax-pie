package mb.pipe.run.eclipse.vfs;

import java.nio.file.Path;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;

import com.google.inject.Inject;

import mb.log.Logger;
import mb.pipe.run.eclipse.util.Nullable;
import mb.vfs.path.PPath;
import mb.vfs.path.PathSrvImpl;

public class EclipsePathSrvImpl extends PathSrvImpl implements EclipsePathSrv {
    private final Logger logger;
    private final IWorkspaceRoot root;


    @Inject public EclipsePathSrvImpl(Logger logger) {
        this.logger = logger.forContext(getClass());
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }


    @Override public PPath resolveWorkspaceRoot() {
        return resolve(root);
    }

    @Override public @Nullable PPath resolve(IResource resource) {
        IPath path = resource.getRawLocation();
        if(path == null) {
            path = resource.getLocation();
        }
        if(path == null) {
            return null;
        }
        return resolve(path);
    }

    @Override public PPath resolve(IPath path) {
        return resolveLocal(path.toFile());
    }

    @Override public @Nullable PPath resolve(IEditorInput input) {
        if(input instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) input;
            return resolve(fileInput.getFile());
        } else if(input instanceof IPathEditorInput) {
            final IPathEditorInput pathInput = (IPathEditorInput) input;
            return resolve(pathInput.getPath());
        } else if(input instanceof IURIEditorInput) {
            final IURIEditorInput uriInput = (IURIEditorInput) input;
            return resolve(uriInput.getURI());
        } else if(input instanceof IStorageEditorInput) {
            final IStorageEditorInput storageInput = (IStorageEditorInput) input;
            final IStorage storage;
            try {
                storage = storageInput.getStorage();
            } catch(CoreException e) {
                return null;
            }

            final IPath path = storage.getFullPath();
            if(path != null) {
                return resolve(path);
            }
        }
        logger.error("Could not resolve editor input {}", input);
        return null;
    }

    @Override public @Nullable IResource unresolve(PPath pipePath) {
        final Path path = pipePath.getJavaPath();
        final IPath eclipsePath = org.eclipse.core.runtime.Path.fromOSString(path.toString());
        IResource resource = root.getFileForLocation(eclipsePath);
        if(resource == null) {
            resource = root.getContainerForLocation(eclipsePath);
        }
        return resource;
    }
}
