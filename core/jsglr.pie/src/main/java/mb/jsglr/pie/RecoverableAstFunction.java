package mb.jsglr.pie;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import org.spoofax.interpreter.terms.IStrategoTerm;

class RecoverableAstFunction extends MapperFunction<Result<JsglrParseOutput, JsglrParseException>, Result<IStrategoTerm, JsglrParseException>> {
    public static final RecoverableAstFunction instance = new RecoverableAstFunction();

    @Override
    public Result<IStrategoTerm, JsglrParseException> apply(Result<JsglrParseOutput, JsglrParseException> result) {
        return result.map(r -> r.ast);
    }

    private RecoverableAstFunction() {}

    private Object readResolve() { return instance; }
}
