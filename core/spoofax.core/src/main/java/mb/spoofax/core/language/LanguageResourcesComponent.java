package mb.spoofax.core.language;

import mb.resource.ResourceRegistry;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.ResourceServiceModule;

import java.util.Set;

public interface LanguageResourcesComponent {
    Set<ResourceRegistry> getResourceRegistries();

    default void addResourceRegistriesTo(ResourceServiceModule resourceServiceModule) {
        resourceServiceModule.addRegistriesFrom(this);
    }

    default void addResourceRegistriesTo(BaseResourceServiceModule resourceServiceModule) {
        resourceServiceModule.addRegistriesFrom(this);
    }
}
