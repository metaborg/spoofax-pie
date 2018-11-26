package mb.spoofax.runtime.eclipse.util;

import com.google.inject.Inject;
import java.net.URI;
import java.util.Arrays;
import mb.fs.java.JavaFSNode;
import mb.fs.java.JavaFSPath;
import mb.log.api.Logger;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.*;

public class FileUtils {
    private final Logger logger;
    private final IWorkspaceRoot root;


    @Inject public FileUtils(Logger logger) {
        this.logger = logger.forContext(getClass());
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }


    public JavaFSPath workspaceRootPath() {
        return toPath(root);
    }

    public @Nullable JavaFSPath toPath(IResource resource) {
        IPath path = resource.getRawLocation();
        if(path == null) {
            path = resource.getLocation();
        }
        if(path == null) {
            return null;
        }
        return toPath(path);
    }

    public JavaFSPath toPath(IPath path) {
        return new JavaFSPath(path.toFile());
    }

    public JavaFSPath toPath(URI uri) {
        return new JavaFSPath(uri);
    }

    public @Nullable JavaFSPath toPath(IEditorInput input) {
        if(input instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) input;
            return toPath(fileInput.getFile());
        } else if(input instanceof IPathEditorInput) {
            final IPathEditorInput pathInput = (IPathEditorInput) input;
            return toPath(pathInput.getPath());
        } else if(input instanceof IURIEditorInput) {
            final IURIEditorInput uriInput = (IURIEditorInput) input;
            return toPath(uriInput.getURI());
        } else if(input instanceof IStorageEditorInput) {
            final IStorageEditorInput storageInput = (IStorageEditorInput) input;
            final IStorage storage;
            try {
                storage = storageInput.getStorage();
            } catch(@SuppressWarnings("unused") CoreException e) {
                return null;
            }

            final IPath path = storage.getFullPath();
            if(path != null) {
                return toPath(path);
            }
        }
        logger.error("Could not resolve editor input {}", input);
        return null;
    }



    public JavaFSNode workspaceRootNode() {
        return toNode(root);
    }

    public @Nullable JavaFSNode toNode(IResource resource) {
        final @Nullable JavaFSPath path = toPath(resource);
        if(path == null) {
            return null;
        }
        return path.toNode();
    }

    public JavaFSNode toNode(IPath path) {
        return new JavaFSNode(path.toFile());
    }

    public JavaFSNode toNode(URI uri) {
        return new JavaFSNode(uri);
    }

    public @Nullable JavaFSNode toNode(IEditorInput input) {
        final @Nullable JavaFSPath path = toPath(input);
        if(path == null) {
            return null;
        }
        return path.toNode();
    }


    public @Nullable IResource toResource(JavaFSPath path) {
        final IPath eclipsePath = org.eclipse.core.runtime.Path.fromOSString(path.toString());
        IResource resource = root.getFileForLocation(eclipsePath);
        if(resource == null) {
            resource = root.getContainerForLocation(eclipsePath);
        }
        return resource;
    }

    public @Nullable IResource toResource(JavaFSNode node) {
        return toResource(node.getPath());
    }


    public @Nullable IFile toFile(IPath path) {
        return root.getFileForLocation(path);
    }

    public @Nullable IFile toFile(URI uri) {
        final IFile[] files = root.findFilesForLocationURI(uri);
        if(files == null || files.length == 0) {
            return null;
        }
        if(files.length > 1) {
            logger.warn("Found multiple matching files {} for URI {}, returning first", Arrays.toString(files), uri);
        }
        return files[0];
    }

    public @Nullable IFile toFile(IStorage storage) {
        final IPath path = storage.getFullPath();
        if(path == null) {
            return null;
        }
        return toFile(path);
    }

    public @Nullable IFile toFile(IEditorInput input) {
        if(input instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) input;
            return fileInput.getFile();
        } else if(input instanceof IPathEditorInput) {
            final IPathEditorInput pathInput = (IPathEditorInput) input;
            final IPath path = pathInput.getPath();
            return toFile(path);
        } else if(input instanceof IURIEditorInput) {
            final IURIEditorInput uriInput = (IURIEditorInput) input;
            final URI uri = uriInput.getURI();
            return toFile(uri);
        } else if(input instanceof IStorageEditorInput) {
            final IStorageEditorInput storageInput = (IStorageEditorInput) input;
            final IStorage storage;
            try {
                storage = storageInput.getStorage();
                return toFile(storage);
            } catch(CoreException e) {
                return null;
            }
        }
        logger.error("Could not get Eclipse file for editor input {}", input);
        return null;
    }
}
