package mb.statix.multilang.eclipse;

import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.MultiLangComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class MultiLangPlugin extends Plugin {
    public static final String id = "statix.multilang.eclipse";

    private static @Nullable MultiLangPlugin plugin;
    private static @Nullable MultiLangComponent component;

    public static MultiLangPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException(
                "Cannot access MultiLangPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static MultiLangComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access MultiLangComponent; MultiLangPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        component = DaggerMultiLangComponent
            .builder()
            .platformComponent(SpoofaxPlugin.getComponent())
            .build();

        // Initialize extension point
        AnalysisContextInitializer.execute(Platform.getExtensionRegistry());
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }
}
