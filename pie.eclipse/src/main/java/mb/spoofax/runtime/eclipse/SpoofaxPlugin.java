package mb.spoofax.runtime.eclipse;

import mb.log.LogModule;
import mb.pie.lang.runtime.PieLangRuntimeModule;
import mb.pie.taskdefs.guice.GuiceTaskDefsModule;
import mb.spoofax.api.*;
import mb.spoofax.legacy.StaticSpoofaxCoreFacade;
import mb.spoofax.pie.SpoofaxPieModule;
import mb.spoofax.pie.SpoofaxPieTaskDefsModule;
import mb.spoofax.pie.generated.TaskDefsModule_spoofax;
import mb.spoofax.runtime.SpoofaxRuntimeModule;
import mb.spoofax.runtime.eclipse.pipeline.PipelineProjectManager;
import mb.spoofax.runtime.eclipse.util.LoggingConfiguration;
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

public class SpoofaxPlugin extends AbstractUIPlugin implements IStartup {
    public static final String id = "mb.spoofax.runtime.eclipse";

    private static volatile SpoofaxPlugin plugin;
    private static volatile Logger logger;
    private static volatile SpoofaxFacade spoofaxFacade;
    private static volatile SpoofaxMeta spoofaxCoreMetaFacade;
    private static volatile Spoofax spoofaxCoreFacade;
    private static volatile boolean doneLoading;

    public static final boolean useInMemoryStore = true;


    @Override public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // Initialize logging
        LoggingConfiguration.configure(SpoofaxPlugin.class, "/logback.xml");
        logger = LoggerFactory.getLogger(SpoofaxPlugin.class);
        logger.debug("Starting Spoofax plugin");

        // Initialize Spoofax Core
        try {
            spoofaxCoreFacade = new Spoofax(new SpoofaxEclipseModule(), new SpoofaxExtensionModule());
            spoofaxCoreMetaFacade = new SpoofaxMeta(spoofaxCoreFacade);
            StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade);
        } catch(MetaborgException e) {
            logger.error("Instantiating Spoofax Core failed", e);
            throw e;
        }

        // Initialize Spoofax runtime
        spoofaxFacade = new SpoofaxFacade(new SpoofaxModule(), new LogModule(logger), new EclipseModule(),
            new EclipseVFSModule(), new SpoofaxRuntimeModule(), new SpoofaxPieModule(), new GuiceTaskDefsModule(),
            new SpoofaxPieTaskDefsModule(), new PieLangRuntimeModule(), new TaskDefsModule_spoofax());
        StaticSpoofaxFacade.init(spoofaxFacade);
        spoofaxFacade.injector.getInstance(PipelineProjectManager.class).initialize();

        doneLoading = true;
    }

    @Override public void stop(BundleContext context) throws Exception {
        logger.debug("Stopping Spoofax plugin");
        doneLoading = false;
        spoofaxFacade = null;
        spoofaxFacade = null;
        spoofaxCoreMetaFacade = null;
        spoofaxCoreFacade.close();
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


    public static SpoofaxPlugin plugin() {
        return plugin;
    }

    public static SpoofaxFacade spoofaxFacade() {
        return spoofaxFacade;
    }

    public static Spoofax spoofaxCoreFacade() {
        return spoofaxCoreFacade;
    }

    public static boolean doneLoading() {
        return doneLoading;
    }

    public static ImageRegistry imageRegistry() {
        return plugin.getImageRegistry();
    }
}
