package mb.pipe.run.eclipse;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mb.ceres.CeresModule;
import mb.pipe.run.ceres.PipeCeresModule;
import mb.pipe.run.ceres.generated.CeresBuilderModule;
import mb.pipe.run.core.PipeEx;
import mb.pipe.run.core.PipeFacade;
import mb.pipe.run.core.PipeModule;
import mb.pipe.run.core.StaticPipeFacade;
import mb.pipe.run.eclipse.util.LoggingConfiguration;
import mb.pipe.run.spoofax.PipeSpoofaxModule;
import mb.pipe.run.spoofax.util.StaticSpoofax;

public class PipePlugin extends AbstractUIPlugin implements IStartup {
    public static final String id = "mb.pipe.run.eclipse";

    private static volatile PipePlugin plugin;
    private static volatile Logger logger;
    private static volatile PipeFacade pipeFacade;
    private static volatile Spoofax spoofaxFacade;
    private static volatile SpoofaxMeta spoofaxMetaFacade;
    private static volatile boolean doneLoading;


    @Override public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        LoggingConfiguration.configure(PipePlugin.class, "/logback.xml");

        logger = LoggerFactory.getLogger(PipePlugin.class);
        logger.debug("Starting Pipe plugin");

        try {
            pipeFacade = new PipeFacade(new PipeModule(logger), new PipeEclipseModule(logger), new EclipseModule(),
                new PipeSpoofaxModule(), new CeresModule(), new CeresBuilderModule(), new PipeCeresModule());
            StaticPipeFacade.init(pipeFacade);
        } catch(PipeEx e) {
            logger.error("Instantiating Pipe failed", e);
            throw e;
        }

        try {
            spoofaxFacade = new Spoofax(new SpoofaxExtensionModule());
            spoofaxMetaFacade = new SpoofaxMeta(spoofaxFacade);
            StaticSpoofax.init(spoofaxMetaFacade);
        } catch(MetaborgException e) {
            logger.error("Instantiating Spoofax failed", e);
            throw e;
        }

        doneLoading = true;
    }

    @Override public void stop(BundleContext context) throws Exception {
        logger.debug("Stopping Spoofax plugin");
        doneLoading = false;
        spoofaxMetaFacade = null;
        spoofaxFacade.close();
        spoofaxFacade = null;
        pipeFacade = null;
        logger = null;
        plugin = null;
        super.stop(context);
    }

    @Override public void earlyStartup() {
        /*
         * Ignore early startup, but this forces this plugin to be started when Eclipse starts. This is required for
         * setting up editor associations for language components and dialects in plugins as soon as possible.
         */
    }


    public static PipePlugin plugin() {
        return plugin;
    }

    public static PipeFacade pipeFacade() {
        return pipeFacade;
    }

    public static Spoofax spoofaxFacade() {
        return spoofaxFacade;
    }

    public static boolean doneLoading() {
        return doneLoading;
    }

    public static ImageRegistry imageRegistry() {
        return plugin.getImageRegistry();
    }
}
