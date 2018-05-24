package mb.spoofax.runtime.eclipse.vfs;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;

import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.vfs.path.PPath;
import mb.vfs.path.PathSrv;

public interface EclipsePathSrv extends PathSrv {
    PPath resolveWorkspaceRoot();

    @Nullable PPath resolve(IResource resource);

    PPath resolve(IPath path);

    @Nullable PPath resolve(IEditorInput input);


    @Nullable IFile toFile(IPath path);

    @Nullable IFile toFile(URI uri);

    @Nullable IFile toFile(IStorage storage);

    @Nullable IFile toFile(IEditorInput input);


    @Nullable IResource unresolve(PPath pipePath);
}
