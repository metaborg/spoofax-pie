package mb.jsglr1.pie;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

class RecoverableAstFunction extends MapperFunction<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<IStrategoTerm, JSGLR1ParseException>> {
    public static final RecoverableAstFunction instance = new RecoverableAstFunction();

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.map(r -> r.ast);
    }

    private RecoverableAstFunction() {}

    private Object readResolve() { return instance; }
}
