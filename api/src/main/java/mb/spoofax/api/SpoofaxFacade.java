package mb.spoofax.api;

import com.google.common.collect.Lists;
import com.google.inject.*;
import java.util.Collection;
import mb.pie.vfs.path.PathSrv;

public class SpoofaxFacade {
    public final Injector injector;
    public final PathSrv pathSrv;


    public SpoofaxFacade(SpoofaxModule module, Module... additionalModules) {
        final Collection<Module> modules = Lists.newArrayList(additionalModules);
        modules.add(module);
        this.injector = Guice.createInjector(Stage.PRODUCTION, modules);
        this.pathSrv = injector.getInstance(PathSrv.class);
    }
}
