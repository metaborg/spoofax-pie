package mb.spoofax.runtime.model;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import mb.log.LogModule;
import mb.log.Logger;
import mb.vfs.VFSModule;
import mb.vfs.path.PathSrv;

public class SpoofaxFacade {
    public final Injector injector;

    public final Logger rootLogger;

    public final PathSrv pathSrv;


    public SpoofaxFacade(SpoofaxModule module, LogModule logModule, VFSModule vfsModule, Module... additionalModules)
        throws SpoofaxEx {
        final Collection<Module> modules = Lists.newArrayList(additionalModules);
        modules.add(module);
        modules.add(logModule);
        modules.add(vfsModule);

        this.injector = Guice.createInjector(Stage.PRODUCTION, modules);

        this.rootLogger = injector.getInstance(Logger.class);

        this.pathSrv = injector.getInstance(PathSrv.class);
    }
}
