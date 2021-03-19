package mb.spoofax.lwb.eclipse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxLwbPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.lwb.eclipse";

    private static @Nullable SpoofaxLwbPlugin plugin;

    public static SpoofaxLwbPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access SpoofaxLwbPlugin instance; plugin has not been started yet, or has been stopped");
        }
        return plugin;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }
}
