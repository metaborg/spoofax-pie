package mb.vfs;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.vfs.path.PathSrv;
import mb.vfs.path.PathSrvImpl;

public class VFSModule extends AbstractModule {
    @Override protected void configure() {
        bind(PathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(PathSrvImpl.class).in(Singleton.class);
    }
}
