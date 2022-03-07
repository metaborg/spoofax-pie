package mb.spoofax.eclipse;

import mb.log.api.Logger;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.component.StaticComponentManagerBuilder;
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

import java.util.ArrayList;

public class SpoofaxPlugin extends AbstractUIPlugin implements IStartup {
    public static final String id = "spoofax.eclipse";
    public static final String participantExtensionPointId = "spoofax.eclipse.participant";

    private static @Nullable SpoofaxPlugin plugin;
    private static @Nullable EclipseLoggerComponent loggerComponent;
    private static @Nullable Logger logger;
    private static @Nullable EclipseResourceServiceComponent baseResourceServiceComponent;
    private static @Nullable EclipsePlatformComponent platformComponent;
    private static @Nullable StaticComponentManager staticComponentManager;
    private static @Nullable ComponentManager componentManager;


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

    public static StaticComponentManager getStaticComponentManager() {
        if(staticComponentManager == null) {
            throw new RuntimeException("Cannot access StaticComponentManager; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return staticComponentManager;
    }

    public static ComponentManager getComponentManager() {
        if(componentManager == null) {
            throw new RuntimeException("Cannot access ComponentManager; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return componentManager;
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

        final StaticComponentManagerBuilder<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> builder =
            new StaticComponentManagerBuilder<>(loggerComponent, baseResourceServiceComponent, platformComponent, PieBuilderImpl::new);
        gatherParticipants(builder, logger);
        staticComponentManager = builder.build();
        if(componentManager == null) {
            componentManager = staticComponentManager;
        }
    }

    @Override public void earlyStartup() {
        // Force early startup.
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(staticComponentManager != null) {
            staticComponentManager.close();
            staticComponentManager = null;
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


    private static ArrayList<EclipseParticipant> gatherParticipants(
        StaticComponentManagerBuilder<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> builder,
        Logger logger
    ) {
        final ArrayList<EclipseParticipant> participants = new ArrayList<>();
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final @Nullable IExtensionPoint point = registry.getExtensionPoint(participantExtensionPointId);
        if(point == null) {
            logger.error("Cannot gather Spoofax component creation participants, extension point {} does not exist", participantExtensionPointId);
            return participants;
        }
        for(IExtension extension : point.getExtensions()) {
            addParticipants(builder, logger, extension);
        }
        return participants;
    }

    private static void addParticipants(
        StaticComponentManagerBuilder<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> builder,
        Logger logger,
        IExtension extension
    ) {
        final IContributor contributor = extension.getContributor();
        for(IConfigurationElement config : extension.getConfigurationElements()) {
            if(config.getName().equals("participant")) {
                try {
                    final Object participantObject = config.createExecutableExtension("class");
                    if(participantObject == null) {
                        logger.error("Found participant in '{}', but it does not have a 'class' property; skipping...", contributor);
                        continue;
                    }
                    if(participantObject instanceof EclipseParticipant) {
                        final EclipseParticipant eclipseParticipant = (EclipseParticipant)participantObject;
                        builder.registerParticipant(eclipseParticipant);
                    } else {
                        logger.error("Found participant in '{}', but the object '{}' instantiated from its 'class' property does not implement 'EclipseParticipant'; skipping...", contributor, participantObject);
                    }
                } catch(CoreException | InvalidRegistryObjectException e) {
                    logger.error("Found participant in '{}', but instantiating an object of its 'class' property failed unexpectedly; skipping...", e, contributor);
                }
            }
        }
    }

    // HACK: allow overriding of the component manager (for dynamic loading)
    public static void setComponentManager(ComponentManager componentManager) {
        SpoofaxPlugin.componentManager = componentManager;
    }
}
