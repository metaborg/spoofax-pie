package mb.pipe.run.eclipse;

import org.slf4j.Logger;

import com.google.inject.Singleton;

import mb.pipe.run.core.PipeModule;
import mb.pipe.run.core.path.PathSrv;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;
import mb.pipe.run.eclipse.vfs.EclipsePathSrvImpl;

public class PipeEclipseModule extends PipeModule {
    public PipeEclipseModule(Logger rootLogger) {
        super(rootLogger);
    }


    @Override protected void bindPath() {
        bind(EclipsePathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(EclipsePathSrvImpl.class);
        bind(EclipsePathSrv.class).to(EclipsePathSrvImpl.class);
    }
}
