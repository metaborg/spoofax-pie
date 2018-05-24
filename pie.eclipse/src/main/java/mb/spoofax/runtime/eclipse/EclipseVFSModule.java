package mb.spoofax.runtime.eclipse;

import com.google.inject.Singleton;

import mb.pie.vfs.path.PathSrv;
import mb.spoofax.pie.PieVfsModule;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrvImpl;

public class EclipseVFSModule extends PieVfsModule {
    @Override protected void configure() {
        bind(EclipsePathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(EclipsePathSrvImpl.class);
        bind(EclipsePathSrv.class).to(EclipsePathSrvImpl.class);
    }
}
