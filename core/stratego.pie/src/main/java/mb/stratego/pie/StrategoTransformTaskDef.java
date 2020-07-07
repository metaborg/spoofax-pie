package mb.stratego.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;

public abstract class StrategoTransformTaskDef<T> implements TaskDef<Supplier<? extends Result<T, ?>>, Result<IStrategoTerm, ?>> {
    private final ListView<String> strategyNames;

    public StrategoTransformTaskDef(ListView<String> strategyNames) {
        this.strategyNames = strategyNames;
    }

    public StrategoTransformTaskDef(String... strategyNames) {
        this.strategyNames = ListView.of(strategyNames);
    }

    public StrategoTransformTaskDef(String strategyName) {
        this.strategyNames = ListView.of(strategyName);
    }


    protected abstract StrategoRuntime getStrategoRuntime(T input);

    protected abstract IStrategoTerm getAst(T input);

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, Supplier<? extends Result<T, ?>> supplier) throws IOException {
        return context.require(supplier).flatMapOrElse((t) -> {
            final StrategoRuntime strategoRuntime = getStrategoRuntime(t);
            IStrategoTerm ast = getAst(t);
            for(String strategyName : strategyNames) {
                try {
                    ast = strategoRuntime.invoke(strategyName, ast);
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }
            return Result.ofOk(ast);
        }, Result::ofErr);
    }
}
