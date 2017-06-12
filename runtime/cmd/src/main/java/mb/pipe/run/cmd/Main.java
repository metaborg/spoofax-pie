package mb.pipe.run.cmd;

import mb.ceres.CeresModule;
import mb.pipe.run.ceres.PipeCeresModule;
import mb.pipe.run.ceres.generated.CeresBuilderModule;
import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.PipeModule;
import mb.pipe.run.core.StaticPipeFacade;
import mb.pipe.run.spoofax.PipeSpoofaxModule;
import mb.pipe.run.spoofax.util.StaticSpoofax;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    public static void main(String[] args) throws Throwable {
        final Logger rootLogger = LoggerFactory.getLogger("root");
        final PipeFacade pipeFacade = new PipeFacade(new PipeModule(rootLogger), new PipeCmdModule(),
            new PipeSpoofaxModule(), new CeresModule(), new CeresBuilderModule(), new PipeCeresModule());
        StaticPipeFacade.init(pipeFacade);

        try (final Spoofax spoofax = new Spoofax(new NullEditorModule());
             final SpoofaxMeta spoofaxMeta = new SpoofaxMeta(spoofax)) {
            StaticSpoofax.init(spoofaxMeta);

            final Runner runner = pipeFacade.injector.getInstance(Runner.class);
            final int result = runner.run(args);
            System.exit(result);
        }
    }
}
