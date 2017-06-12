package mb.pipe.run.eclipse.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

import mb.pipe.run.core.path.VFSManagerProvider;

public class EclipseVFSManagerProvider extends VFSManagerProvider {
    @Override protected void addDefaultProvider(DefaultFileSystemManager manager) throws FileSystemException {
        final EclipseFileProvider provider = new EclipseFileProvider();
        manager.addProvider("eclipse", provider);
        manager.setDefaultProvider(provider);
    }

    @Override protected void setBaseFile(DefaultFileSystemManager manager) throws FileSystemException {
        manager.setBaseFile(manager.resolveFile("eclipse:///"));
    }

    @Override protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        super.addProviders(manager);
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("bundleresource", new EclipseBundleResourceProvider());
    }
}
