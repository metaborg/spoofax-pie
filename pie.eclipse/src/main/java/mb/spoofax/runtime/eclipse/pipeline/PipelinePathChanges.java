package mb.spoofax.runtime.eclipse.pipeline;

import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Inject;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;

public class PipelinePathChanges {
    private final EclipsePathSrv pathSrv;


    @Inject public PipelinePathChanges(EclipsePathSrv pathSrv) {
        this.pathSrv = pathSrv;
    }


    public HashSet<PPath> changedPaths(@Nullable IResourceDelta delta) throws CoreException {
        final HashSet<PPath> changedPaths = new HashSet<>();
        if(delta == null) {
            return changedPaths;
        }
        delta.accept(new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta innerDelta) throws CoreException {
                final int kind = innerDelta.getKind();
                switch(kind) {
                    case IResourceDelta.ADDED:
                    case IResourceDelta.REMOVED:
                    case IResourceDelta.CHANGED: {
                        final IResource resource = innerDelta.getResource();
                        if(!(resource.getType() != IResource.FILE && kind == IResourceDelta.CHANGED)) {
                            final PPath path = pathSrv.resolve(resource);
                            changedPaths.add(path);
                        }
                        break;
                    }
                    case IResourceDelta.NO_CHANGE:
                    case IResourceDelta.ADDED_PHANTOM:
                    case IResourceDelta.REMOVED_PHANTOM:
                    default:
                        break;
                }
                return true;
            }
        });
        return changedPaths;
    }
}
