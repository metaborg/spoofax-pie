package mb.spoofax.runtime.model;

import com.google.common.collect.Lists;
import com.google.inject.*;
import mb.log.LogModule;
import mb.log.Logger;
import mb.pie.vfs.path.PathSrv;

import java.util.Collection;

public class SpoofaxFacade {
    public final Injector injector;

    public final Logger rootLogger;

    public final PathSrv pathSrv;


    public SpoofaxFacade(SpoofaxModule module, LogModule logModule, Module... additionalModules)
        throws SpoofaxEx {
        final Collection<Module> modules = Lists.newArrayList(additionalModules);
        modules.add(module);
        modules.add(logModule);

        this.injector = Guice.createInjector(Stage.PRODUCTION, modules);

        this.rootLogger = injector.getInstance(Logger.class);

        this.pathSrv = injector.getInstance(PathSrv.class);
    }
}
