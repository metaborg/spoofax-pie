package mb.spoofax.intellij;

import mb.log.noop.NoopLoggerFactory;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.intellij.log.IntellijLoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxPlugin {
    private static @Nullable SpoofaxIntellijComponent component;

    public static SpoofaxIntellijComponent getComponent() {
        if(component == null) {
            init();
        }
        return component;
    }

    public static void init() {
        component = DaggerSpoofaxIntellijComponent
            .builder()
            .loggerFactoryModule(new LoggerFactoryModule(new IntellijLoggerFactory()))
            .pieModule(new PieModule(PieBuilderImpl::new))
            .build();
    }
}
