package mb.pipe.run.core;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.log.SLF4JLogger;
import mb.pipe.run.core.path.PathSrv;
import mb.pipe.run.core.path.PathSrvImpl;

public class PipeModule extends AbstractModule {
    private final org.slf4j.Logger rootLogger;


    public PipeModule(org.slf4j.Logger rootLogger) {
        this.rootLogger = rootLogger;
    }


    @Override protected void configure() {
        bindLog();
        bindResource();
    }


    protected void bindLog() {
        final Logger pipeRootLogger = new SLF4JLogger(rootLogger);
        bind(Logger.class).toInstance(pipeRootLogger);
    }

    protected void bindResource() {
        bind(PathSrvImpl.class).in(Singleton.class);
        bind(PathSrv.class).to(PathSrvImpl.class).in(Singleton.class);
    }
}
