package mb.spt.model;

import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.language.LanguageComponent;
import mb.spt.api.model.LanguageUnderTest;

public class LanguageUnderTestImpl implements LanguageUnderTest {
    private final ResourceServiceComponent resourceServiceComponent;
    private final LanguageComponent languageComponent;
    private final PieComponent pieComponent;

    public LanguageUnderTestImpl(
        ResourceServiceComponent resourceServiceComponent,
        LanguageComponent languageComponent,
        PieComponent pieComponent
    ) {
        this.resourceServiceComponent = resourceServiceComponent;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
    }

    @Override public ResourceServiceComponent getResourceServiceComponent() {
        return resourceServiceComponent;
    }

    @Override public LanguageComponent getLanguageComponent() {
        return languageComponent;
    }

    @Override public PieComponent getPieComponent() {
        return pieComponent;
    }
}
