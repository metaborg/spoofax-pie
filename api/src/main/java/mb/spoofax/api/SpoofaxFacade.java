package mb.spoofax.api;

import com.google.common.collect.Lists;
import com.google.inject.*;
import java.util.Collection;
import mb.pie.vfs.path.PathSrv;

public class SpoofaxFacade {
    public final Injector injector;

    public SpoofaxFacade(Module... modules) {
        final Collection<Module> allModules = Lists.newArrayList(modules);
        this.injector = Guice.createInjector(Stage.PRODUCTION, allModules);
    }
}
