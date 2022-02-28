package mb.spoofax.lwb.eclipse;

import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.lwb.dynamicloading.DaggerDynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingModule;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManagerBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxLwbPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.lwb.eclipse";

    private static @Nullable BundleContext bundleContext;
    private static @Nullable SpoofaxLwbPlugin plugin;
    private static @Nullable DynamicLoadingComponent dynamicLoadingComponent;
    private static @Nullable DynamicComponentManager dynamicComponentManager;

    public static SpoofaxLwbPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access SpoofaxLwbPlugin instance; SpoofaxLwbPlugin has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static BundleContext getBundleContext() {
        if(bundleContext == null) {
            throw new RuntimeException("Cannot access BundleContext instance; SpoofaxLwbPlugin has not been started yet, or has been stopped");
        }
        return bundleContext;
    }

    public static DynamicComponentManager getDynamicComponentManager() {
        if(dynamicComponentManager == null) {
            throw new RuntimeException("Cannot access DynamicComponentManager; SpoofaxLwbPlugin has not been started yet, or has been stopped");
        }
        return dynamicComponentManager;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;
        dynamicLoadingComponent = DaggerDynamicLoadingComponent.builder()
            .dynamicLoadingModule(new DynamicLoadingModule(new DynamicComponentManagerBuilder().build(SpoofaxPlugin.getStaticComponentManager())))
            .build();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        bundleContext = null;
    }
}
