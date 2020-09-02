package mb.spoofax.core.language;

import mb.pie.api.Pie;
import mb.resource.ResourceService;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    ResourceService getResourceService();

    Pie getPie();
}
