package mb.spoofax.runtime.eclipse.pipeline;

import com.google.inject.Inject;
import java.util.HashSet;
import mb.fs.java.JavaFSPath;
import mb.pie.api.ResourceKey;
import mb.pie.api.fs.ResourceKt;
import mb.spoofax.runtime.eclipse.util.FileUtils;
import mb.spoofax.runtime.eclipse.util.Nullable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class PipelinePathChanges {
    private final FileUtils fileUtils;


    @Inject public PipelinePathChanges(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }


    public HashSet<ResourceKey> changedPaths(@Nullable IResourceDelta delta) throws CoreException {
        final HashSet<ResourceKey> changedResources = new HashSet<>();
        if(delta == null) {
            return changedResources;
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
                            final JavaFSPath path = fileUtils.toPath(resource);
                            final ResourceKey resourceKey = ResourceKt.toResourceKey(path);
                            changedResources.add(resourceKey);
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
        return changedResources;
    }
}
