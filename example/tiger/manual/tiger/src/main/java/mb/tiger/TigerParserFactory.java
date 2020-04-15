package mb.tiger;

import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.interfaces.spoofaxcore.ParserFactory;

public class TigerParserFactory implements ParserFactory {
    private final TigerParseTable parseTable;

    public TigerParserFactory(HierarchicalResource definitionDir) {
        this.parseTable = TigerParseTable.fromDefinitionDir(definitionDir);
    }

    @Override public TigerParser create() {
        return new TigerParser(parseTable);
    }
}
