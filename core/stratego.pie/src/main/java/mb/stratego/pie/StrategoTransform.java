package mb.stratego.pie;

import mb.common.result.Result;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class StrategoTransform {
    public static Result<IStrategoTerm, ?> exec(
        IStrategoTerm ast,
        StrategoRuntime strategoRuntime,
        Iterable<String> strategyNames
    ) {
        for(String strategyName : strategyNames) {
            try {
                ast = strategoRuntime.invoke(strategyName, ast);
            } catch(StrategoException e) {
                return Result.ofErr(e);
            }
        }
        return Result.ofOk(ast);
    }
}
