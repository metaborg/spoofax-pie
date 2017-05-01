package mb.pipe.run.eclipse;

import org.apache.commons.vfs2.FileSystemManager;
import org.slf4j.Logger;

import com.google.inject.Singleton;

import mb.pipe.run.core.PipeModule;
import mb.pipe.run.core.vfs.IResourceSrv;
import mb.pipe.run.core.vfs.IVfsSrv;
import mb.pipe.run.eclipse.vfs.EclipseResourceSrv;
import mb.pipe.run.eclipse.vfs.EclipseVFSManagerProvider;
import mb.pipe.run.eclipse.vfs.IEclipseResourceSrv;

public class PipeEclipseModule extends PipeModule {
    public PipeEclipseModule(Logger rootLogger) {
        super(rootLogger);
    }


    @Override protected void bindResource() {
        bind(EclipseResourceSrv.class).in(Singleton.class);
        bind(IResourceSrv.class).to(EclipseResourceSrv.class);
        bind(IEclipseResourceSrv.class).to(EclipseResourceSrv.class);
        bind(IVfsSrv.class).to(EclipseResourceSrv.class).in(Singleton.class);

        bind(FileSystemManager.class).toProvider(EclipseVFSManagerProvider.class).in(Singleton.class);
    }
}
