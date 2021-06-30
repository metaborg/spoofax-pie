package mb.spt.model;

import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.language.LanguageComponent;

public interface LanguageUnderTest {
    ResourceServiceComponent getResourceServiceComponent();

    LanguageComponent getLanguageComponent();

    PieComponent getPieComponent();
}
