package mb.spoofax.core.language;

import mb.resource.ResourceService;
import mb.spoofax.core.pie.PieProvider;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    ResourceService getResourceService();

    PieProvider getPieProvider();
}
