package mb.pipe.run.core;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.pipe.run.core.path.PathSrv;
import mb.pipe.run.core.path.PathSrvImpl;

public class VFSModule extends AbstractModule {
    @Override protected void configure() {
        bind(PathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(PathSrvImpl.class).in(Singleton.class);
    }
}
