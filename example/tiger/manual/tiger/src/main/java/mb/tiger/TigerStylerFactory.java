package mb.tiger;

import mb.log.api.LoggerFactory;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.StylerFactory;

public class TigerStylerFactory implements StylerFactory {
    private final TigerStylingRules stylingRules;
    private final LoggerFactory loggerFactory;

    public TigerStylerFactory(LoggerFactory loggerFactory, HierarchicalResource definitionDir) {
        this.stylingRules = TigerStylingRules.fromDefinitionDir(definitionDir);
        this.loggerFactory = loggerFactory;
    }

    @Override public TigerStyler create() {
        return new TigerStyler(stylingRules, loggerFactory);
    }
}
