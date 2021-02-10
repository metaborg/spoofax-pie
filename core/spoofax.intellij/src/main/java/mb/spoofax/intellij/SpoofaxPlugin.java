package mb.spoofax.intellij;

import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.intellij.log.IntellijLoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxPlugin {
    private static @Nullable IntellijResourceServiceComponent resourceServiceComponent;
    private static @Nullable IntellijPlatformComponent platformComponent;

    public static IntellijResourceServiceComponent getResourceServiceComponent() {
        if(resourceServiceComponent == null) {
            init();
        }
        return resourceServiceComponent;
    }

    public static IntellijPlatformComponent getPlatformComponent() {
        if(platformComponent == null) {
            init();
        }
        return platformComponent;
    }

    public static void init() {
        resourceServiceComponent = DaggerIntellijResourceServiceComponent.create();
        platformComponent = DaggerIntellijPlatformComponent
            .builder()
            .loggerFactoryModule(new LoggerFactoryModule(new IntellijLoggerFactory()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .intellijResourceServiceComponent(resourceServiceComponent)
            .build();
    }
}
