package mb.pipe.run.cmd;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mb.log.LogModule;
import mb.pipe.run.ceres.PipeCeresModule;
import mb.pipe.run.ceres.generated.CeresBuilderModule_clang;
import mb.pipe.run.ceres.generated.CeresBuilderModule_spoofax;
import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.PipeModule;
import mb.pipe.run.core.StaticPipeFacade;
import mb.pipe.run.spoofax.PipeSpoofaxModule;
import mb.pipe.run.spoofax.util.StaticSpoofax;
import mb.vfs.VFSModule;

public final class Main {
    public static void main(String[] args) throws Throwable {
        final Logger rootLogger = LoggerFactory.getLogger("root");
        final PipeFacade pipeFacade = new PipeFacade(new PipeModule(), new LogModule(rootLogger), new VFSModule(),
            new PipeCmdModule(), new PipeSpoofaxModule(), new PipeCeresModule(), new CeresBuilderModule_clang(),
            new CeresBuilderModule_spoofax());
        StaticPipeFacade.init(pipeFacade);

        try(final Spoofax spoofax = new Spoofax(new NullEditorModule(), new SpoofaxExtensionModule());
            final SpoofaxMeta spoofaxMeta = new SpoofaxMeta(spoofax)) {
            StaticSpoofax.init(spoofaxMeta);

            final Runner runner = pipeFacade.injector.getInstance(Runner.class);
            final int result = runner.run(args);
            System.exit(result);
        }
    }
}
