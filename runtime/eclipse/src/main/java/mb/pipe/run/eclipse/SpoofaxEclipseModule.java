package mb.pipe.run.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.eclipse.resource.EclipseFileSystemManagerProvider;
import org.metaborg.spoofax.eclipse.resource.EclipseResourceService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;

import com.google.inject.Singleton;

public class SpoofaxEclipseModule extends SpoofaxModule {
    @Override protected void bindResource() {
        bind(EclipseResourceService.class).in(Singleton.class);
        bind(IResourceService.class).to(EclipseResourceService.class);
        bind(IEclipseResourceService.class).to(EclipseResourceService.class);

        bind(FileSystemManager.class).toProvider(EclipseFileSystemManagerProvider.class).in(Singleton.class);
    }
}
