package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.common.JsglrParseOutput;

public interface Parser {
    JsglrParseOutput parse(JsglrParseInput input) throws JsglrParseException, InterruptedException;
}
