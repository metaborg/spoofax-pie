package mb.jsglr1.pie;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

class AstFunction extends MapperFunction<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Result<IStrategoTerm, JSGLR1ParseException>> {
    public static final AstFunction instance = new AstFunction();

    @Override
    public Result<IStrategoTerm, JSGLR1ParseException> apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(JSGLR1ParseException.recoveryDisallowedFail(r.messages, r.startSymbol, r.fileHint, r.rootDirectoryHint));
            } else {
                return Result.ofOk(r.ast);
            }
        });
    }

    private AstFunction() {}

    private Object readResolve() { return instance; }
}
