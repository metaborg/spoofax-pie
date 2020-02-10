package mb.tiger;

import mb.spoofax.compiler.interfaces.spoofaxcore.ParserFactory;

public class TigerParserFactory implements ParserFactory {
    private final TigerParseTable parseTable;

    public TigerParserFactory() {
        this.parseTable = TigerParseTable.fromClassLoaderResources();
    }

    @Override public TigerParser create() {
        return new TigerParser(parseTable);
    }
}
