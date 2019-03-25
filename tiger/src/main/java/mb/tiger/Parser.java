package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.common.JSGLR1Parser;

public class Parser {
    private final JSGLR1Parser internalParser;

    public Parser(ParseTable parseTable) {
        this.internalParser = new JSGLR1Parser(parseTable.internalParseTable);
    }

    public JSGLR1ParseOutput parse(String text, String startSymbol) throws InterruptedException {
        return internalParser.parse(text, startSymbol);
    }
}
