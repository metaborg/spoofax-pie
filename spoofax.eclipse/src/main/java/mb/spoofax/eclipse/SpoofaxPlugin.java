package mb.spoofax.eclipse;

import mb.log.noop.NoopLoggerFactory;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.platform.LoggerFactoryModule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SpoofaxPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.eclipse";

    private static @Nullable SpoofaxEclipseComponent component;

    public static SpoofaxEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access SpoofaxEclipseComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        component = DaggerSpoofaxEclipseComponent
            .builder()
            .loggerFactoryModule(new LoggerFactoryModule(new NoopLoggerFactory()))
            .pieModule(new PieModule(PieBuilderImpl::new))
            .build();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(component != null) {
            component.getColorShare().dispose();
        }
    }
}
