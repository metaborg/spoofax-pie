package mb.jsglr.pie;

import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;

class RecoverableTokensFunction extends MapperFunction<Result<JsglrParseOutput, JsglrParseException>, Result<JSGLRTokens, JsglrParseException>> {
    public static final RecoverableTokensFunction instance = new RecoverableTokensFunction();

    @Override
    public Result<JSGLRTokens, JsglrParseException> apply(Result<JsglrParseOutput, JsglrParseException> result) {
        return result.map(r -> r.tokens);
    }

    private RecoverableTokensFunction() {}

    private Object readResolve() { return instance; }
}
