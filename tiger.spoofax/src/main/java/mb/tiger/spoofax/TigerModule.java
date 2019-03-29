package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.tiger.TigerParseTable;
import mb.tiger.TigerParser;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylingRules;

import java.io.IOException;

@Module
public class TigerModule {
    private final TigerParseTable parseTable = TigerParseTable.fromClassLoaderResources();
    private final TigerParser parser = new TigerParser(parseTable);
    private final TigerStylingRules stylingRules = TigerStylingRules.fromClassLoaderResources();
    private final TigerStyler styler = new TigerStyler(stylingRules);

    public TigerModule() throws IOException, JSGLR1ParseTableException {}

    @Provides TigerParser provideParser() {
        return parser;
    }

    @Provides TigerStyler styler() {
        return styler;
    }
}
