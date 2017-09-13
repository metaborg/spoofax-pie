package mb.pipe.run.eclipse;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxExtensionModule;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mb.log.LogModule;
import mb.pie.runtime.builtin.PieBuiltinModule;
import mb.pipe.run.eclipse.util.LoggingConfiguration;
import mb.spoofax.runtime.impl.legacy.StaticSpoofaxCoreFacade;
import mb.spoofax.runtime.model.SpoofaxEx;
import mb.spoofax.runtime.model.SpoofaxFacade;
import mb.spoofax.runtime.model.SpoofaxModule;
import mb.spoofax.runtime.model.StaticSpoofaxFacade;
import mb.spoofax.runtime.pie.SpoofaxPieModule;
import mb.spoofax.runtime.pie.generated.PieBuilderModule_spoofax;

public class PipePlugin extends AbstractUIPlugin implements IStartup {
    public static final String id = "mb.pipe.run.eclipse";

    private static volatile PipePlugin plugin;
    private static volatile Logger logger;
    private static volatile SpoofaxFacade spoofaxFacade;
    private static volatile Spoofax spoofaxCoreFacade;
    private static volatile SpoofaxMeta spoofaxCoreMetaFacade;
    private static volatile boolean doneLoading;


    @Override public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // Initialize logging
        LoggingConfiguration.configure(PipePlugin.class, "/logback.xml");
        logger = LoggerFactory.getLogger(PipePlugin.class);
        logger.debug("Starting Pipe plugin");

        // Initialize Spoofax runtime
        try {
            spoofaxFacade = new SpoofaxFacade(new SpoofaxModule(), new LogModule(logger), new EclipseVFSModule(),
                new EclipseModule(), new SpoofaxPieModule(), new PieBuiltinModule(), new PieBuilderModule_spoofax());
            StaticSpoofaxFacade.init(spoofaxFacade);
        } catch(SpoofaxEx e) {
            logger.error("Instantiating Spoofax PIE failed", e);
            throw e;
        }

        // Initialize Spoofax Core
        try {
            spoofaxCoreFacade = new Spoofax(new SpoofaxPipeModule(), new SpoofaxExtensionModule());
            spoofaxCoreMetaFacade = new SpoofaxMeta(spoofaxCoreFacade);
            StaticSpoofaxCoreFacade.init(spoofaxCoreMetaFacade);
        } catch(MetaborgException e) {
            logger.error("Instantiating Spoofax Core failed", e);
            throw e;
        }

        // Load meta-languages
        final ILanguageDiscoveryService langDiscoverSrv = spoofaxCoreFacade.languageDiscoveryService;
        final String spoofaxDir = "/Users/gohla/spoofax";

        // final String spoofaxRelengMasterDir = spoofaxDir + "/master/repo/spoofax-releng";
        // langDiscoverSrv
        // .languageFromDirectory(spoofaxFacade.resolve(spoofaxRelengMasterDir + "/esv/org.metaborg.meta.lang.esv"));
        // langDiscoverSrv.languageFromDirectory(
        // spoofaxFacade.resolve(spoofaxRelengMasterDir + "/sdf/org.metaborg.meta.lang.template"));
        // langDiscoverSrv
        // .languageFromDirectory(spoofaxFacade.resolve(spoofaxRelengMasterDir + "/spoofax/meta.lib.spoofax"));

        // Load baseline meta-languages
        final String spoofaxRelengReleaseDir = spoofaxDir + "/release/repo/spoofax-releng";
        langDiscoverSrv.languageFromDirectory(
            spoofaxCoreFacade.resolve(spoofaxRelengReleaseDir + "/esv/org.metaborg.meta.lang.esv"));
        langDiscoverSrv.languageFromDirectory(
            spoofaxCoreFacade.resolve(spoofaxRelengReleaseDir + "/sdf/org.metaborg.meta.lang.template"));
        langDiscoverSrv
            .languageFromDirectory(spoofaxCoreFacade.resolve(spoofaxRelengReleaseDir + "/spoofax/meta.lib.spoofax"));

        doneLoading = true;
    }

    @Override public void stop(BundleContext context) throws Exception {
        logger.debug("Stopping Spoofax plugin");
        doneLoading = false;
        spoofaxCoreMetaFacade = null;
        spoofaxCoreFacade.close();
        spoofaxFacade = null;
        spoofaxFacade = null;
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
