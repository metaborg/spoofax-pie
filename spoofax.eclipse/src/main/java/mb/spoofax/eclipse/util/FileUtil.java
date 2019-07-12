package mb.spoofax.eclipse.util;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.Arrays;

@Singleton
public class FileUtil {
    private final Logger logger;
    private final IWorkspaceRoot root;

    @Inject public FileUtil(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.create(getClass());
        this.root = ResourcesPlugin.getWorkspace().getRoot();
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
        final @Nullable IPath path = storage.getFullPath();
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
