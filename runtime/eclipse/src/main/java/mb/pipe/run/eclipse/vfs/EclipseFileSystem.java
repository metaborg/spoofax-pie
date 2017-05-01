package mb.pipe.run.eclipse.vfs;

import java.io.File;
import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class EclipseFileSystem extends AbstractFileSystem {
    private final IWorkspaceRoot root;


    public EclipseFileSystem(FileName rootName, FileObject parentLayer, FileSystemOptions fileSystemOptions) {
        super(rootName, parentLayer, fileSystemOptions);
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }


    @Override protected FileObject createFile(AbstractFileName name) throws Exception {
        return new EclipseFileObject(name, root, this);
    }

    @Override protected void addCapabilities(Collection<Capability> caps) {
        caps.addAll(EclipseFileProvider.capabilities);
    }

    @Override protected File doReplicateFile(FileObject file, FileSelector selector) throws Exception {
        final EclipseFileObject eclipseResource = (EclipseFileObject) file;
        final IResource resource = eclipseResource.resource();
        if(resource == null) {
            return super.doReplicateFile(file, selector);
        }
        IPath path = resource.getRawLocation();
        if(path == null) {
            path = resource.getLocation();
        }
        if(path == null) {
            return super.doReplicateFile(file, selector);
        }
        return path.makeAbsolute().toFile();
    }
}
