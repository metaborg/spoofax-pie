package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import mb.common.util.IterableUtil;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.url.URLResourceRegistry;
import mb.spoofax.core.language.LanguageResourcesComponent;

import javax.inject.Named;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Module
public class BaseResourceServiceModule {
    private final Set<ResourceRegistry> registries;


    public BaseResourceServiceModule(Set<ResourceRegistry> registries) {
        this.registries = registries;
    }

    public BaseResourceServiceModule(ResourceRegistry... registries) {
        this(new HashSet<>(Arrays.asList(registries)));
    }

    public BaseResourceServiceModule() {
        this(new HashSet<>());
    }


    public BaseResourceServiceModule addRegistry(ResourceRegistry registry) {
        registries.add(registry);
        return this;
    }

    public BaseResourceServiceModule addRegistries(Iterable<ResourceRegistry> registries) {
        IterableUtil.addAll(this.registries, registries);
        return this;
    }

    public BaseResourceServiceModule addRegistriesFrom(LanguageResourcesComponent languageResourcesComponent) {
        IterableUtil.addAll(registries, languageResourcesComponent.getResourceRegistries());
        return this;
    }


    @Provides @ResourceServiceScope
    static FSResourceRegistry provideDefaultResourceRegistry() {
        return new FSResourceRegistry();
    }

    @Provides @Named("default") @ResourceServiceScope
    static Optional<ResourceRegistry> provideDefaultResourceRegistryAsOptional(FSResourceRegistry registry) {
        return Optional.of(registry);
    }

    @Provides @ResourceServiceScope @IntoSet
    static ResourceRegistry provideDefaultResourceRegistryIntoSet(FSResourceRegistry registry) {
        return registry;
    }


    @Provides @ResourceServiceScope
    static URLResourceRegistry provideUrlResourceRegistry() {
        return new URLResourceRegistry();
    }

    @Provides @ResourceServiceScope @IntoSet
    static ResourceRegistry provideUrlResourceRegistryIntoSet(URLResourceRegistry registry) {
        return registry;
    }


    @Provides @ResourceServiceScope @ElementsIntoSet
    Set<ResourceRegistry> provideResourceRegistries() {
        return registries;
    }


    @Provides @Named("parent") @ResourceServiceScope
    static Optional<ResourceService> provideParentResourceService() {
        return Optional.empty();
    }
}
