package mb.pipe.run.core;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import mb.pipe.run.core.vfs.IVfsSrv;
import mb.pipe.run.core.vfs.IResourceSrv;

public class PipeFacade {
    public final Injector injector;

    public final IResourceSrv resourceSrv;
    public final IVfsSrv fileObjectSrv;


    public PipeFacade(PipeModule module, Module... additionalModules) throws PipeEx {
        final Collection<Module> modules = Lists.newArrayList(additionalModules);
        modules.add(module);

        final Injector injector = Guice.createInjector(modules);
        this.injector = injector;

        this.resourceSrv = injector.getInstance(IResourceSrv.class);
        this.fileObjectSrv = injector.getInstance(IVfsSrv.class);
    }
}
