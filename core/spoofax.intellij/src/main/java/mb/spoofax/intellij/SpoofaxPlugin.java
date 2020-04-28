package mb.spoofax.intellij;

import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
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
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .build();
    }
}
