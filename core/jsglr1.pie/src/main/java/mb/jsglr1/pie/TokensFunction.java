package mb.jsglr1.pie;

import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;

class TokensFunction extends MapperFunction<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<JSGLRTokens, JSGLR1ParseException>> {
    public static final TokensFunction instance = new TokensFunction();

    @Override
    public Result<JSGLRTokens, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(JSGLR1ParseException.recoveryDisallowedFail(r.messages, r.startSymbol, r.fileHint, r.rootDirectoryHint));
            } else {
                return Result.ofOk(r.tokens);
            }
        });
    }

    private TokensFunction() {}

    private Object readResolve() { return instance; }
}
