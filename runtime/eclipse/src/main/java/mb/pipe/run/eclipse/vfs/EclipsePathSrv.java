package mb.pipe.run.eclipse.vfs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;

import mb.pipe.run.core.path.PPath;
import mb.pipe.run.core.path.PathSrv;
import mb.pipe.run.eclipse.util.Nullable;

public interface EclipsePathSrv extends PathSrv {
    PPath resolveWorkspaceRoot();

    @Nullable PPath resolve(IResource resource);

    PPath resolve(IPath path);

    @Nullable PPath resolve(IEditorInput input);

    @Nullable IResource unresolve(PPath pipePath);
}
