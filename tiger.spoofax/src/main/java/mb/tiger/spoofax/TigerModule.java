package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerParseTable;
import mb.tiger.TigerParser;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylingRules;

import java.io.IOException;

@Module
public class TigerModule {
    private final TigerParser parser;
    private final TigerStyler styler;

    private TigerModule(TigerParseTable parseTable, TigerStylingRules stylingRules) {
        this.parser = new TigerParser(parseTable);
        this.styler = new TigerStyler(stylingRules);
    }

    public static TigerModule fromClassLoaderResources() throws JSGLR1ParseTableException, IOException {
        final TigerParseTable parseTable = TigerParseTable.fromClassLoaderResources();
        final TigerStylingRules stylingRules = TigerStylingRules.fromClassLoaderResources();
        return new TigerModule(parseTable, stylingRules);
    }

    @LanguageScope @Provides TigerParser provideParser() {
        return parser;
    }

    @LanguageScope @Provides TigerStyler styler() {
        return styler;
    }
}
