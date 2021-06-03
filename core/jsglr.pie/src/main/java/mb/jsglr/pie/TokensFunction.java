package mb.jsglr.pie;

import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;

class TokensFunction extends MapperFunction<Result<JsglrParseOutput, JsglrParseException>, Result<JSGLRTokens, JsglrParseException>> {
    public static final TokensFunction instance = new TokensFunction();

    @Override
    public Result<JSGLRTokens, JsglrParseException> apply(Result<JsglrParseOutput, JsglrParseException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(JsglrParseException.recoveryDisallowedFail(r.messages, r.startSymbol, r.fileHint, r.rootDirectoryHint));
            } else {
                return Result.ofOk(r.tokens);
            }
        });
    }

    private TokensFunction() {}

    private Object readResolve() { return instance; }
}
