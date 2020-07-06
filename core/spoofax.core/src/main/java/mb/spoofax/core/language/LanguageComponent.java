package mb.spoofax.core.language;

import mb.pie.api.Pie;
import mb.resource.ResourceService;

import javax.inject.Provider;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    ResourceService getResourceService();

    Provider<Pie> getPieProvider();
}
