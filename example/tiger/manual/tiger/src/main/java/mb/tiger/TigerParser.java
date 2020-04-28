package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1Parser;
import mb.resource.ResourceKey;
import mb.spoofax.compiler.interfaces.spoofaxcore.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TigerParser implements Parser {
    private final JSGLR1Parser parser;

    public TigerParser(TigerParseTable parseTable) {
        this.parser = new JSGLR1Parser(parseTable.parseTable);
    }

    @Override
    public JSGLR1ParseResult parse(String text, String startSymbol) throws InterruptedException {
        return parser.parse(text, startSymbol, null);
    }

    @Override
    public JSGLR1ParseResult parse(String text, String startSymbol, @Nullable ResourceKey resource) throws InterruptedException {
        return parser.parse(text, startSymbol, resource);
    }
}
