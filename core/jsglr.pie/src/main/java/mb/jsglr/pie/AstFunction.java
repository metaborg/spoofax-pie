package mb.jsglr.pie;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

class AstFunction extends MapperFunction<Result<JsglrParseOutput, JsglrParseException>, Result<IStrategoTerm, JsglrParseException>> {
    public static final AstFunction instance = new AstFunction();

    @Override
    public Result<IStrategoTerm, JsglrParseException> apply(Result<JsglrParseOutput, JsglrParseException> result) {
        return result.flatMap(r -> {
            if(r.recovered) {
                return Result.ofErr(JsglrParseException.recoveryDisallowedFail(r.messages, r.startSymbol, r.fileHint, r.rootDirectoryHint));
            } else {
                return Result.ofOk(r.ast);
            }
        });
    }

    private AstFunction() {}

    private Object readResolve() { return instance; }
}
