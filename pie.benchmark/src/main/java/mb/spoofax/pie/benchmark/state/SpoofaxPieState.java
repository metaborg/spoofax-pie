package mb.spoofax.pie.benchmark.state;

import com.google.inject.Injector;
import mb.log.LogModule;
import mb.log.Logger;
import mb.pie.lang.runtime.PieLangRuntimeModule;
import mb.pie.vfs.path.PathSrv;
import mb.spoofax.api.*;
import mb.spoofax.legacy.StaticSpoofaxCoreFacade;
import mb.spoofax.pie.*;
import mb.spoofax.pie.benchmark.SpoofaxCoreModule;
import mb.spoofax.pie.generated.TaskDefsModule_spoofax;
import mb.spoofax.runtime.SpoofaxRuntimeModule;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.LoggerFactory;

@State(Scope.Benchmark)
public class SpoofaxPieState {
    public final SpoofaxFacade spoofaxFacade;
    public final Spoofax spoofaxCoreFacade;
    public final SpoofaxMeta spoofaxCoreMetaFacade;
    public final Injector injector;
    public final Logger logger;
    public final PathSrv pathSrv;
    public final SpoofaxPipeline spoofaxPipeline;


    public SpoofaxPieState() {
        try {
            spoofaxFacade =
                new SpoofaxFacade(new SpoofaxModule(), new LogModule(LoggerFactory.getLogger("root")), new SpoofaxRuntimeModule(),
                    new SpoofaxPieModule(), new PieVfsModule(), new SpoofaxPieTaskDefsModule(), new PieLangRuntimeModule(),
                    new TaskDefsModule_spoofax());
            StaticSpoofaxFacade.init(spoofaxFacade);
            spoofaxCoreFacade = new Spoofax(new SpoofaxCoreModule(), new SpoofaxExtensionModule());
            spoofaxCoreMetaFacade = new SpoofaxMeta(spoofaxCoreFacade);
            StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade);
            injector = spoofaxFacade.injector;
            logger = injector.getInstance(Logger.class);
            pathSrv = injector.getInstance(PathSrv.class);
            spoofaxPipeline = injector.getInstance(SpoofaxPipeline.class);
        } catch(MetaborgException e) {
            throw new RuntimeException(e);
        }
    }
}
