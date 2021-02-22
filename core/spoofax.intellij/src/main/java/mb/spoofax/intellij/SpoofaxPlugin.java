package mb.spoofax.intellij;

import mb.spoofax.intellij.log.DaggerIntellijLoggerComponent;
import mb.spoofax.intellij.log.IntellijLoggerComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxPlugin {
    private static @Nullable IntellijLoggerComponent loggerComponent;
    private static @Nullable IntellijResourceServiceComponent resourceServiceComponent;
    private static @Nullable IntellijPlatformComponent platformComponent;

    public static IntellijLoggerComponent getLoggerComponent() {
        if(loggerComponent == null) {
            init();
        }
        return loggerComponent;
    }

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
        loggerComponent = DaggerIntellijLoggerComponent.create();
        resourceServiceComponent = DaggerIntellijResourceServiceComponent.builder()
            .intellijLoggerComponent(loggerComponent)
            .build();
        platformComponent = DaggerIntellijPlatformComponent.builder()
            .intellijLoggerComponent(loggerComponent)
            .intellijResourceServiceComponent(resourceServiceComponent)
            .build();
    }
}
