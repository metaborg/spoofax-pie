package mb.spoofax.intellij;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.intellij.log.IntellijLoggerComponent;

@PlatformScope
@Component(
    modules = {

    },
    dependencies = {
        IntellijLoggerComponent.class,
        IntellijResourceServiceComponent.class
    }
)
public interface IntellijPlatformComponent extends PlatformComponent {

}
