package mb.pipe.run.cmd;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.PipeModule;
import mb.pipe.run.core.StaticPipeFacade;
import mb.pipe.run.spoofax.util.StaticSpoofax;

public final class Main {
    public static void main(String[] args) throws Throwable {
        try(final Spoofax spoofax = new Spoofax(new NullEditorModule());
            final SpoofaxMeta spoofaxMeta = new SpoofaxMeta(spoofax, new Module())) {
            final Injector injector = spoofaxMeta.injector;
            SpoofaxContext.init(injector);
            StaticSpoofax.init(spoofaxMeta);

            final Logger rootLogger = LoggerFactory.getLogger("root");
            final PipeFacade pipe = new PipeFacade(new PipeModule(rootLogger));
            StaticPipeFacade.init(pipe);

            final Runner runner = injector.getInstance(Runner.class);
            final int result = runner.run(args);
            System.exit(result);
        }
    }
}
