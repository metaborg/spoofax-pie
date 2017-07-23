package mb.pipe.run.eclipse;

import com.google.inject.Singleton;

import mb.pipe.run.core.path.PathSrv;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;
import mb.pipe.run.eclipse.vfs.EclipsePathSrvImpl;

public class EclipseVFSModule extends mb.pipe.run.core.VFSModule {
    @Override protected void configure() {
        bind(EclipsePathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(EclipsePathSrvImpl.class);
        bind(EclipsePathSrv.class).to(EclipsePathSrvImpl.class);
    }
}
