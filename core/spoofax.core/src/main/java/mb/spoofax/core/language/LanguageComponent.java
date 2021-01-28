package mb.spoofax.core.language;

import mb.pie.api.Pie;
import mb.resource.ResourceService;

public interface LanguageComponent {
    ResourceService getResourceService();

    Pie getPie();

    LanguageInstance getLanguageInstance();
}
