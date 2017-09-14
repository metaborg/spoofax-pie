package mb.spoofax.runtime.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.eclipse.resource.EclipseFileSystemManagerProvider;

import com.google.inject.Singleton;

public class SpoofaxEclipseModule extends SpoofaxModule {
    protected void bindResource() {
        bind(ResourceService.class).in(Singleton.class);
        bind(IResourceService.class).to(ResourceService.class);
        autoClosableBinder.addBinding().to(ResourceService.class);

        bind(FileSystemManager.class).toProvider(EclipseFileSystemManagerProvider.class).in(Singleton.class);
    }
}
