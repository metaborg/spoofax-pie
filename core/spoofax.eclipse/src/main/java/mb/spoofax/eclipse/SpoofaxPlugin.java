package mb.spoofax.eclipse;

import mb.common.util.MultiMap;
import mb.log.api.Logger;
import mb.spoofax.eclipse.log.DaggerEclipseLoggerComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxPlugin extends AbstractUIPlugin implements IStartup {
    public static final String id = "spoofax.eclipse";
    public static final String extensionPointId = "spoofax.eclipse.lifecycle";

    private static @Nullable SpoofaxPlugin plugin;
    private static @Nullable EclipseLoggerComponent loggerComponent;
    private static @Nullable Logger logger;
    private static @Nullable EclipseResourceServiceComponent baseResourceServiceComponent;
    private static @Nullable EclipsePlatformComponent platformComponent;
    private static @Nullable LifecycleParticipantManager lifecycleParticipantManager;


    public static SpoofaxPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access SpoofaxPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static EclipseLoggerComponent getLoggerComponent() {
        if(loggerComponent == null) {
            throw new RuntimeException("Cannot access EclipseLoggerComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return loggerComponent;
    }

    public static EclipseResourceServiceComponent getBaseResourceServiceComponent() {
        if(baseResourceServiceComponent == null) {
            throw new RuntimeException("Cannot access EclipseResourceServiceComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return baseResourceServiceComponent;
    }

    public static EclipsePlatformComponent getPlatformComponent() {
        if(platformComponent == null) {
            throw new RuntimeException("Cannot access EclipsePlatformComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return platformComponent;
    }

    public static LifecycleParticipantManager getLifecycleParticipantManager() {
        if(lifecycleParticipantManager == null) {
            throw new RuntimeException("Cannot access LifecycleParticipantManager; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return lifecycleParticipantManager;
    }


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        loggerComponent = DaggerEclipseLoggerComponent.create();
        logger = loggerComponent.getLoggerFactory().create(getClass());
        baseResourceServiceComponent = DaggerEclipseResourceServiceComponent.builder()
            .eclipseLoggerComponent(loggerComponent)
            .build();
        platformComponent = DaggerEclipsePlatformComponent.builder()
            .eclipseLoggerComponent(loggerComponent)
            .eclipseResourceServiceComponent(baseResourceServiceComponent)
            .build();
        platformComponent.init();
        lifecycleParticipantManager = new LifecycleParticipantManager(loggerComponent, baseResourceServiceComponent, platformComponent);
        lifecycleParticipantManager.registerStatic(gatherLifecycleParticipants(logger));
    }

    @Override public void earlyStartup() {
        // Force early startup.
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(lifecycleParticipantManager != null) {
            lifecycleParticipantManager.close();
            lifecycleParticipantManager = null;
        }
        if(platformComponent != null) {
            platformComponent.close();
            platformComponent = null;
        }
        if(baseResourceServiceComponent != null) {
            baseResourceServiceComponent.close();
            baseResourceServiceComponent = null;
        }
        logger = null;
        loggerComponent = null;
        plugin = null;
    }


    private static MultiMap<String, EclipseLifecycleParticipant> gatherLifecycleParticipants(Logger logger) {
        final MultiMap<String, EclipseLifecycleParticipant> lifecycleParticipants = MultiMap.withLinkedHash();
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
        for(IExtension extension : point.getExtensions()) {
            addLifecycleParticipants(logger, extension, lifecycleParticipants);
        }
        return lifecycleParticipants;
    }

    private static void addLifecycleParticipants(
        Logger logger,
        IExtension extension,
        MultiMap<String, EclipseLifecycleParticipant> lifecycleParticipants
    ) {
        final IContributor contributor = extension.getContributor();
        for(IConfigurationElement config : extension.getConfigurationElements()) {
            if(config.getName().equals("participant")) {
                final String group = config.getAttribute("group");
                if(group == null) {
                    logger.error("Found participant in '{}', but it does not have a 'group' attribute; skipping...", contributor);
                    continue;
                }
                try {
                    final Object lifecycleParticipantObject = config.createExecutableExtension("class");
                    if(lifecycleParticipantObject == null) {
                        logger.error("Found participant in '{}', but it does not have a 'class' property; skipping...", contributor);
                    }
                    if(lifecycleParticipantObject instanceof EclipseLifecycleParticipant) {
                        final EclipseLifecycleParticipant eclipseLifecycleParticipant = (EclipseLifecycleParticipant)lifecycleParticipantObject;
                        lifecycleParticipants.put(group, eclipseLifecycleParticipant);
                    } else {
                        logger.error("Found participant in '{}', but the object '{}' instantiated from its 'class' property does not implement 'EclipseLifecycleParticipant'; skipping...", contributor, lifecycleParticipantObject);
                    }
                } catch(CoreException | InvalidRegistryObjectException e) {
                    logger.error("Found participant in '{}', but instantiating an object of its 'class' property failed unexpectedly; skipping...", e, contributor);
                }
            }
        }
    }
}
