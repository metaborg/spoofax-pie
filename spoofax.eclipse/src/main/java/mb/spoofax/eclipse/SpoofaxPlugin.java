package mb.spoofax.eclipse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxPlugin extends AbstractUIPlugin {
    public static final String pluginId = "spoofax.eclipse";

    public static EclipseComponent component;

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);

        component = DaggerEclipseComponent
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
    }
}
