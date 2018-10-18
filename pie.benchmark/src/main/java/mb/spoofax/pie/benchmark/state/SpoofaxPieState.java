package mb.spoofax.pie.benchmark.state;

import com.google.inject.Injector;
import mb.log.api.Logger;
import mb.log.slf4j.LogModule;
import mb.pie.lang.runtime.PieLangRuntimeModule;
import mb.pie.vfs.path.PathSrv;
import mb.spoofax.api.SpoofaxFacade;
import mb.spoofax.api.StaticSpoofaxFacade;
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
    public final Spoofax spoofaxCoreFacade;
    public final SpoofaxMeta spoofaxCoreMetaFacade;
    public final SpoofaxFacade spoofaxFacade;
    public final Injector injector;
    public final Logger logger;
    public final PathSrv pathSrv;
    public final SpoofaxPipeline spoofaxPipeline;


    public SpoofaxPieState() {
        try {
            spoofaxCoreFacade = new Spoofax(new SpoofaxCoreModule(), new SpoofaxExtensionModule());
            spoofaxCoreMetaFacade = new SpoofaxMeta(spoofaxCoreFacade);
            StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade);

            spoofaxFacade = new SpoofaxFacade(
                new SpoofaxRuntimeModule(), // Spoofax runtime (implementation)
                new LogModule(LoggerFactory.getLogger("root")), // SLF4J logging support
                new PieVfsModule(), // PIE VFS support
                new PieLangRuntimeModule(), // PIE DSL task definitions
                new SpoofaxPieModule(), // Spoofax-PIE support
                new SpoofaxPieTaskDefsModule(), // Spoofax-PIE task definitions
                new TaskDefsModule_spoofax() // Spoofax-PIE generated task definitions
            );
            StaticSpoofaxFacade.init(spoofaxFacade);

            injector = spoofaxFacade.injector;
            logger = injector.getInstance(Logger.class);
            pathSrv = injector.getInstance(PathSrv.class);
            spoofaxPipeline = injector.getInstance(SpoofaxPipeline.class);
        } catch(MetaborgException e) {
            throw new RuntimeException(e);
        }
    }
}
