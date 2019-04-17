package mb.tiger.eclipse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class TigerPlugin extends AbstractUIPlugin {
    public static final String pluginId = "tiger.eclipse";

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
    }
}
