package mb.pipe.run.cmd;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;

import com.google.inject.Injector;

import mb.pipe.run.core.util.StaticFacade;

public final class Main {
    public static void main(String[] args) throws Throwable {
        try(final Spoofax spoofax = new Spoofax(new NullEditorModule());
            final SpoofaxMeta spoofaxMeta = new SpoofaxMeta(spoofax, new Module())) {
            final Injector injector = spoofaxMeta.injector;
            SpoofaxContext.init(injector);
            StaticFacade.init(spoofaxMeta);
            final Runner runner = injector.getInstance(Runner.class);
            final int result = runner.run(args);
            System.exit(result);
        }
    }
}
