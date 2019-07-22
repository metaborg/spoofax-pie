package mb.tiger.eclipse;

import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.tiger.spoofax.TigerModule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class TigerPlugin extends AbstractUIPlugin {
    public static final String pluginId = "tiger.eclipse";

    private static @Nullable TigerEclipseComponent component;

    public static TigerEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access TigerComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        component = DaggerTigerEclipseComponent
            .builder()
            .platformComponent(SpoofaxPlugin.getComponent())
            .tigerModule(TigerModule.fromClassLoaderResources())
            .tigerEclipseModule(new TigerEclipseModule())
            .build();
        component.getEditorRegistry().register();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
    }
}
