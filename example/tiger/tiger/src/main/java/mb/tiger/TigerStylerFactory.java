package mb.tiger;

import mb.log.api.LoggerFactory;
import mb.spoofax.compiler.interfaces.spoofaxcore.StylerFactory;

public class TigerStylerFactory implements StylerFactory {
    private final TigerStylingRules stylingRules;
    private final LoggerFactory loggerFactory;

    public TigerStylerFactory(LoggerFactory loggerFactory) {
        this.stylingRules = TigerStylingRules.fromClassLoaderResources();
        this.loggerFactory = loggerFactory;
    }

    @Override public TigerStyler create() {
        return new TigerStyler(stylingRules, loggerFactory);
    }
}
