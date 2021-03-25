package mb.jsglr1.pie;

import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;

class RecoverableTokensFunction extends MapperFunction<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<JSGLRTokens, JSGLR1ParseException>> {
    public static final RecoverableTokensFunction instance = new RecoverableTokensFunction();

    @Override
    public Result<JSGLRTokens, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.map(r -> r.tokens);
    }

    private RecoverableTokensFunction() {}

    private Object readResolve() { return instance; }
}
