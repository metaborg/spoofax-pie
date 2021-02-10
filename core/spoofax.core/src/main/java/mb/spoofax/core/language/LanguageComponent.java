package mb.spoofax.core.language;

import mb.pie.api.Pie;
import mb.resource.ResourceService;

public interface LanguageComponent {
    Pie getPie();

    LanguageInstance getLanguageInstance();
}
