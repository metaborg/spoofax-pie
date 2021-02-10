package mb.spoofax.eclipse;

import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.eclipse.log.EclipseLoggerFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.eclipse";

    private static @Nullable SpoofaxPlugin plugin;
    private static @Nullable EclipseResourceServiceComponent resourceServiceComponent;
    private static @Nullable EclipsePlatformComponent platformComponent;


    public static SpoofaxPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException(
                "Cannot access SpoofaxPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static EclipseResourceServiceComponent getResourceServiceComponent() {
        if(resourceServiceComponent == null) {
            throw new RuntimeException(
                "Cannot access EclipseResourceServiceComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return resourceServiceComponent;
    }

    public static EclipsePlatformComponent getPlatformComponent() {
        if(platformComponent == null) {
            throw new RuntimeException(
                "Cannot access SpoofaxEclipseComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return platformComponent;
    }


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        resourceServiceComponent = DaggerEclipseResourceServiceComponent.create();
        platformComponent = DaggerEclipsePlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(new EclipseLoggerFactory()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .eclipseResourceServiceComponent(resourceServiceComponent)
            .build();
        platformComponent.getPartClosedCallback().register();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(platformComponent != null) {
            platformComponent.getColorShare().dispose();
            platformComponent = null;
        }
        resourceServiceComponent = null;
        plugin = null;
    }
}
