package org.metaborg.spoofax.eclipse.resource;

import java.io.File;
import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.core.resource.ResourceService;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import mb.pipe.run.core.path.FileUtils;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.vfs.EclipseFileObject;

public class EclipseResourceService extends ResourceService implements IEclipseResourceService {
    private final IWorkspaceRoot root;


    @Inject public EclipseResourceService(FileSystemManager fileSystemManager,
        @Named("ResourceClassLoader") ClassLoader classLoader) {
        super(fileSystemManager, classLoader);

        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }


    @Override public FileObject resolve(IResource resource) {
        return resolve(resource.getFullPath());
    }

    @Override public FileObject resolve(IPath path) {
        return resolve("eclipse://" + path.toString());
    }

    @Override public FileObject resolveWorkspaceRoot() {
        return resolve(root);
    }

    @Override public @Nullable FileObject resolve(IEditorInput input) {
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
            } else {
                try {
                    final FileObject ramFile = resolve("ram://eclipse/" + input.getName());
                    return ramFile;
                } catch(MetaborgRuntimeException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override public @Nullable ResourceChange resolve(IResourceDelta delta) {
        final FileObject resource = resolve(delta.getResource());
        final int eclipseKind = delta.getKind();
        final ResourceChangeKind kind;
        // GTODO: handle move/copies better
        switch(eclipseKind) {
            case IResourceDelta.NO_CHANGE:
                return null;
            case IResourceDelta.ADDED:
                kind = ResourceChangeKind.Create;
                break;
            case IResourceDelta.REMOVED:
                kind = ResourceChangeKind.Delete;
                break;
            case IResourceDelta.CHANGED:
                kind = ResourceChangeKind.Modify;
                break;
            default:
                final String message = String.format("Unhandled resource delta type: %s", eclipseKind);
                throw new MetaborgRuntimeException(message);
        }

        return new ResourceChange(resource, kind);
    }

    @Override public @Nullable IResource unresolve(FileObject resource) {
        if(resource instanceof EclipseFileObject) {
            final EclipseFileObject eclipseResource = (EclipseFileObject) resource;
            try {
                return eclipseResource.resource();
            } catch(Exception e) {
                return null;
            }
        }

        if(resource instanceof EclipseResourceFileObject) {
            final EclipseResourceFileObject eclipseResource = (EclipseResourceFileObject) resource;
            try {
                return eclipseResource.resource();
            } catch(Exception e) {
                return null;
            }
        }

        if(resource instanceof LocalFile) {
            // LEGACY: analysis returns messages with relative local file resources, try to convert as relative first.
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            final String path = resource.getName().getPath();
            IResource eclipseResource = root.findMember(path);
            if(eclipseResource == null) {
                // Path might be absolute, try to get absolute file.
                final URI uri = FileUtils.toURI(resource);
                final IPath location = Path.fromOSString(uri.getPath());
                eclipseResource = root.getFileForLocation(location);
                if(eclipseResource == null) {
                    // If resource is a direct path to a project, getContainerForLocation needs to be used.
                    eclipseResource = root.getContainerForLocation(location);
                }
            }
            return eclipseResource;
        }

        return null;
    }

    @Override public File localFile(FileObject resource) {
        if(!(resource instanceof EclipseResourceFileObject) && !(resource instanceof EclipseFileObject)) {
            return super.localFile(resource);
        }

        final File localResource = localPath(resource);
        if(localResource == null) {
            return super.localFile(resource);
        }
        return super.localFile(resolve(localResource));
    }

    @Override public File localFile(FileObject resource, FileObject dir) {
        if((!(dir instanceof EclipseFileObject) && !(dir instanceof EclipseResourceFileObject))
            || (!(resource instanceof EclipseFileObject) && !(resource instanceof EclipseResourceFileObject))) {
            return super.localFile(resource, dir);
        }

        final File localResource = localPath(resource);
        if(localResource == null) {
            return super.localFile(resource, dir);
        }
        final File localDir = localPath(dir);
        if(localDir == null) {
            return super.localFile(resource, dir);
        }

        return super.localFile(resolve(localResource), resolve(localDir));
    }

    @Override public @Nullable File localPath(FileObject resource) {
        if(!(resource instanceof EclipseResourceFileObject)) {
            return super.localPath(resource);
        }

        try {
            final IResource eclipseResource = unresolve(resource);
            IPath path = eclipseResource.getRawLocation();
            if(path == null) {
                path = eclipseResource.getLocation();
            }
            if(path == null) {
                return null;
            }
            return path.makeAbsolute().toFile();
        } catch(Exception e) {
            return null;
        }
    }
}
