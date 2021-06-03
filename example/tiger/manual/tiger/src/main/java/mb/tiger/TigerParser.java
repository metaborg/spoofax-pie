package mb.tiger;

import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr1.common.JSGLR1Parser;
import mb.spoofax.compiler.interfaces.spoofaxcore.Parser;

public class TigerParser implements Parser {
    private final JSGLR1Parser parser;

    public TigerParser(TigerParseTable parseTable) {
        this.parser = new JSGLR1Parser(parseTable.parseTable);
    }

    @Override public JsglrParseOutput parse(JsglrParseInput input) throws JsglrParseException, InterruptedException {
        return parser.parse(input);
    }
}
