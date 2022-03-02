package mb.spoofax.lwb.eclipse;

import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxLwbPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.lwb.eclipse";

    private static @Nullable BundleContext bundleContext;
    private static @Nullable SpoofaxLwbPlugin plugin;

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

    public static SpoofaxLwbCompilerComponent getSpoofaxLwbCompilerComponent() {
        return SpoofaxPlugin.getStaticComponentManager().getOneSubcomponent(SpoofaxLwbCompilerComponent.class).unwrap();
    }

    public static DynamicLoadingComponent getDynamicLoadingComponent() {
        return SpoofaxPlugin.getStaticComponentManager().getOneSubcomponent(DynamicLoadingComponent.class).unwrap();
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        plugin = this;
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        bundleContext = null;
    }
}
