package mb.spt;

import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.SubcomponentRegistry;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SptParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends BaseSptParticipant<L, R, P> {
    @Override
    public @Nullable ResourceRegistriesProvider getGlobalResourceRegistriesProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        // Provide SPTs resource registry provider as global, because SPT creates its own resources and passes them to
        // language implementations, thus requiring its resource registries to be globally available.
        return getResourceRegistriesProvider(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            subcomponentRegistry,
            dependencyResolver
        );
    }
}
