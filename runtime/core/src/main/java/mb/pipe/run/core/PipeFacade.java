package mb.pipe.run.core;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.path.PathSrv;

public class PipeFacade {
    public final Injector injector;

    public final Logger rootLogger;

    public final PathSrv pathSrv;


    public PipeFacade(PipeModule module, LogModule logModule, VFSModule vfsModule, Module... additionalModules)
        throws PipeEx {
        final Collection<Module> modules = Lists.newArrayList(additionalModules);
        modules.add(module);
        modules.add(logModule);
        modules.add(vfsModule);

        this.injector = Guice.createInjector(modules);

        this.rootLogger = injector.getInstance(Logger.class);

        this.pathSrv = injector.getInstance(PathSrv.class);
    }
}
