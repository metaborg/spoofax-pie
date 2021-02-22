package mb.spoofax.intellij.editor;

import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.SpoofaxPlugin;

public class SpoofaxLexerFactory {
    private final IntellijLanguageComponent languageComponent;
    private final ResourceServiceComponent resourceServiceComponent;
    private final PieComponent pieComponent;

    public SpoofaxLexerFactory(
        IntellijLanguageComponent languageComponent,
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent
    ) {
        this.languageComponent = languageComponent;
        this.resourceServiceComponent = resourceServiceComponent;
        this.pieComponent = pieComponent;
    }

    public SpoofaxLexer create(ResourceKey resourceKey) {
        return new SpoofaxLexer(
            resourceKey,
            SpoofaxPlugin.getLoggerComponent().getLoggerFactory(),
            languageComponent.getTokenTypeManager(),
            languageComponent.getScopeManager(),
            resourceServiceComponent.getResourceService(),
            pieComponent.getPie(),
            languageComponent.getLanguageInstance()
        );
    }
}
