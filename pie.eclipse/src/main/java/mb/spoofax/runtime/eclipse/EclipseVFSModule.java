package mb.spoofax.runtime.eclipse;

import com.google.inject.Singleton;

import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrvImpl;
import mb.vfs.path.PathSrv;

public class EclipseVFSModule extends mb.vfs.VFSModule {
    @Override protected void configure() {
        bind(EclipsePathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(EclipsePathSrvImpl.class);
        bind(EclipsePathSrv.class).to(EclipsePathSrvImpl.class);
    }
}
