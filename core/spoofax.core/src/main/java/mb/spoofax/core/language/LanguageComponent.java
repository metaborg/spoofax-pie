package mb.spoofax.core.language;

import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.core.pie.PieProvider;

import javax.inject.Provider;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    ResourceService getResourceService();

    PieProvider getPieProvider();
}
