package mb.pipe.run.core;

import org.apache.commons.vfs2.FileSystemManager;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.pipe.run.core.log.ILogger;
import mb.pipe.run.core.log.SLF4JLogger;
import mb.pipe.run.core.vfs.IVfsSrv;
import mb.pipe.run.core.vfs.IResourceSrv;
import mb.pipe.run.core.vfs.VFSManagerProvider;
import mb.pipe.run.core.vfs.VFSResourceSrv;

public class PipeModule extends AbstractModule {
    private final Logger rootLogger;


    public PipeModule(Logger rootLogger) {
        this.rootLogger = rootLogger;
    }


    @Override protected void configure() {
        bindLog();
        bindResource();
    }


    protected void bindLog() {
        final ILogger pipeRootLogger = new SLF4JLogger(rootLogger);
        bind(ILogger.class).toInstance(pipeRootLogger);
    }

    protected void bindResource() {
        bind(VFSResourceSrv.class).in(Singleton.class);
        bind(IResourceSrv.class).to(VFSResourceSrv.class).in(Singleton.class);
        bind(IVfsSrv.class).to(VFSResourceSrv.class).in(Singleton.class);

        bind(FileSystemManager.class).toProvider(VFSManagerProvider.class).in(Singleton.class);
    }
}
