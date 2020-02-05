package mb.spoofax.eclipse.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.*;

import java.net.URI;

public class EditorInputUtil {
    public static @Nullable IResource getResource(IEditorInput editorInput) {
        if(editorInput instanceof IFileEditorInput) {
            final IFileEditorInput input = (IFileEditorInput) editorInput;
            return input.getFile();
        } else if(editorInput instanceof IStorageEditorInput) {
            final IStorageEditorInput input = (IStorageEditorInput) editorInput;
            try {
                final @Nullable IPath path = input.getStorage().getFullPath();
                if(path != null) {
                    return getResource(path);
                }
                return null;
            } catch(CoreException e) {
                return null;
            }
        } else if(editorInput instanceof IPathEditorInput) {
            final IPathEditorInput input = (IPathEditorInput) editorInput;
            return getResource(input.getPath());
        } else if(editorInput instanceof IURIEditorInput) {
            final IURIEditorInput input = (IURIEditorInput) editorInput;
            final URI uri = input.getURI();
            final @Nullable IPath path = URIUtil.toPath(uri);
            if(path != null) {
                return getResource(path);
            }
        }
        return null;
    }

    public static @Nullable IFile getFile(IEditorInput editorInput) {
        if(editorInput instanceof IFileEditorInput) {
            final IFileEditorInput input = (IFileEditorInput) editorInput;
            return input.getFile();
        } else if(editorInput instanceof IStorageEditorInput) {
            final IStorageEditorInput input = (IStorageEditorInput) editorInput;
            try {
                final @Nullable IPath path = input.getStorage().getFullPath();
                if(path != null) {
                    return getFile(path);
                }
                return null;
            } catch(CoreException e) {
                return null;
            }
        } else if(editorInput instanceof IPathEditorInput) {
            final IPathEditorInput input = (IPathEditorInput) editorInput;
            return getFile(input.getPath());
        } else if(editorInput instanceof IURIEditorInput) {
            final IURIEditorInput input = (IURIEditorInput) editorInput;
            final URI uri = input.getURI();
            final @Nullable IPath path = URIUtil.toPath(uri);
            if(path != null) {
                return getFile(path);
            }
        }
        return null;
    }

    public static @Nullable IPath getPath(IEditorInput editorInput) {
        if(editorInput instanceof IFileEditorInput) {
            final IFileEditorInput input = (IFileEditorInput) editorInput;
            return input.getFile().getFullPath();
        } else if(editorInput instanceof IStorageEditorInput) {
            final IStorageEditorInput input = (IStorageEditorInput) editorInput;
            try {
                return input.getStorage().getFullPath();
            } catch(CoreException e) {
                return null;
            }
        } else if(editorInput instanceof IPathEditorInput) {
            final IPathEditorInput input = (IPathEditorInput) editorInput;
            return input.getPath();
        } else if(editorInput instanceof IURIEditorInput) {
            final IURIEditorInput input = (IURIEditorInput) editorInput;
            final URI uri = input.getURI();
            return URIUtil.toPath(uri);
        }
        return null;
    }

    public static @Nullable URI getURI(IEditorInput editorInput) {
        if(editorInput instanceof IFileEditorInput) {
            final IFileEditorInput input = (IFileEditorInput) editorInput;
            return input.getFile().getLocationURI();
        } else if(editorInput instanceof IStorageEditorInput) {
            final IStorageEditorInput input = (IStorageEditorInput) editorInput;
            try {
                final @Nullable IPath path = input.getStorage().getFullPath();
                if(path != null) {
                    return URIUtil.toURI(path);
                }
            } catch(CoreException e) {
                return null;
            }
        } else if(editorInput instanceof IPathEditorInput) {
            final IPathEditorInput input = (IPathEditorInput) editorInput;
            return URIUtil.toURI(input.getPath());
        } else if(editorInput instanceof IURIEditorInput) {
            final IURIEditorInput input = (IURIEditorInput) editorInput;
            return input.getURI();
        }
        return null;
    }


    private static @Nullable IResource getResource(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(path);
    }

    private static IFile getFile(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }
}
