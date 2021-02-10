package mb.spoofax.intellij;

import dagger.Component;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.PlatformScope;

@PlatformScope
@Component(
    modules = {
        LoggerFactoryModule.class,
        PlatformPieModule.class
    },
    dependencies = {
        IntellijResourceServiceComponent.class
    }
)
public interface IntellijPlatformComponent extends PlatformComponent {

}
