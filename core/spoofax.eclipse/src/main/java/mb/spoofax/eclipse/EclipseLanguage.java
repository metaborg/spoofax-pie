package mb.spoofax.eclipse;

import mb.spoofax.core.language.LanguageResourcesComponent;
import mb.spoofax.core.platform.ResourceServiceComponent;

public interface EclipseLanguage {
    LanguageResourcesComponent createResourcesComponent();

    EclipseLanguageComponent createComponent(
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    );

    void start(
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    );
}
