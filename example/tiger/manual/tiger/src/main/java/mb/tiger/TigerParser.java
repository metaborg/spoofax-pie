package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseInput;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.common.JSGLR1Parser;
import mb.spoofax.compiler.interfaces.spoofaxcore.Parser;

public class TigerParser implements Parser {
    private final JSGLR1Parser parser;

    public TigerParser(TigerParseTable parseTable) {
        this.parser = new JSGLR1Parser(parseTable.parseTable);
    }

    @Override public JSGLR1ParseOutput parse(JSGLR1ParseInput input) throws JSGLR1ParseException, InterruptedException {
        return parser.parse(input);
    }
}
