package mb.spoofax.runtime.benchmark.state;

import com.google.inject.Injector;
import mb.log.LogModule;
import mb.pie.runtime.builtin.PieBuiltinModule;
import mb.spoofax.runtime.benchmark.SpoofaxCoreModule;
import mb.spoofax.runtime.impl.SpoofaxImplModule;
import mb.spoofax.runtime.impl.legacy.StaticSpoofaxCoreFacade;
import mb.spoofax.runtime.model.SpoofaxEx;
import mb.spoofax.runtime.model.SpoofaxFacade;
import mb.spoofax.runtime.model.SpoofaxModule;
import mb.spoofax.runtime.model.StaticSpoofaxFacade;
import mb.spoofax.runtime.pie.SpoofaxPieModule;
import mb.spoofax.runtime.pie.generated.PieBuilderModule_spoofax;
import mb.vfs.VFSModule;
import mb.vfs.path.PathSrv;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.slf4j.LoggerFactory;

public class SpoofaxPieStaticState {
    public static final SpoofaxFacade spoofaxFacade;
    public static final Spoofax spoofaxCoreFacade;
    public static final SpoofaxMeta spoofaxCoreMetaFacade;

    public static final Injector injector;

    public static final PathSrv pathSrv;

    static {
        try {
            spoofaxFacade =
                new SpoofaxFacade(new SpoofaxModule(), new LogModule(LoggerFactory.getLogger("root")), new VFSModule(),
                    new SpoofaxImplModule(), new SpoofaxPieModule(), new PieBuiltinModule(),
                    new PieBuilderModule_spoofax());
            StaticSpoofaxFacade.init(spoofaxFacade);
            spoofaxCoreFacade = new Spoofax(new SpoofaxCoreModule(), new SpoofaxExtensionModule());
            spoofaxCoreMetaFacade = new SpoofaxMeta(spoofaxCoreFacade);
            StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade);

            injector = spoofaxFacade.injector;

            pathSrv = injector.getInstance(PathSrv.class);
        } catch(SpoofaxEx | MetaborgException e) {
            throw new RuntimeException(e);
        }
    }
}
