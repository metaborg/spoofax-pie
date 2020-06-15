package mb.statix.multilang.eclipse;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class MultiLangPlugin extends Plugin {
    public static final String id = "spoofax.eclipse";

    private static @Nullable MultiLangPlugin plugin;
    // private static @Nullable SpoofaxEclipseComponent component;


    public static MultiLangPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException(
                "Cannot access MultiLangPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    /* public static SpoofaxEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access SpoofaxEclipseComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return component;
    } */


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        /* component = DaggerSpoofaxEclipseComponent
            .builder()
            .loggerFactoryModule(new LoggerFactoryModule(new EclipseLoggerFactory()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .build();

        component.getPartClosedCallback().register();*/
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }
}
