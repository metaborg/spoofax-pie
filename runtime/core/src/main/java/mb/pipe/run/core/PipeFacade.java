package mb.pipe.run.core;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import mb.pipe.run.core.log.ILogger;
import mb.pipe.run.core.vfs.IResourceSrv;
import mb.pipe.run.core.vfs.IVfsSrv;

public class PipeFacade {
    public final Injector injector;

    public final ILogger rootLogger;

    public final IResourceSrv resourceSrv;
    public final IVfsSrv fileObjectSrv;


    public PipeFacade(PipeModule module, Module... additionalModules) throws PipeEx {
        final Collection<Module> modules = Lists.newArrayList(additionalModules);
        modules.add(module);

        final Injector injector = Guice.createInjector(modules);
        this.injector = injector;

        this.rootLogger = injector.getInstance(ILogger.class);

        this.resourceSrv = injector.getInstance(IResourceSrv.class);
        this.fileObjectSrv = injector.getInstance(IVfsSrv.class);
    }
}
