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

/**
 * Abstract task definition class that executes a sequence of Stratego strategies. The Stratego runtime to execute with
 * is provided by {@link #getStrategoRuntime(ExecContext, Object)}, and the initial AST to apply it to with {@link
 * #getAst(ExecContext, Object)}.
 *
 * This task definition is normally used through {@link ProviderStrategoTransformTaskDef} and transitively through
 * {@link AstStrategoTransformTaskDef}.
 *
 * @param <T> Type of inputs to this task definition.
 */
public abstract class StrategoTransformTaskDef<T> implements TaskDef<Supplier<? extends Result<T, ?>>, Result<IStrategoTerm, ?>> {
    private final ListView<String> strategyNames;

    public StrategoTransformTaskDef(ListView<String> strategyNames) {
        this.strategyNames = strategyNames;
    }

    public StrategoTransformTaskDef(String... strategyNames) {
        this.strategyNames = ListView.of(strategyNames);
    }


    protected abstract StrategoRuntime getStrategoRuntime(ExecContext context, T input);

    protected abstract IStrategoTerm getAst(ExecContext context, T input);

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, Supplier<? extends Result<T, ?>> supplier) throws IOException {
        return context.require(supplier).flatMapOrElse((t) -> {
            final StrategoRuntime strategoRuntime = getStrategoRuntime(context, t);
            IStrategoTerm ast = getAst(context, t);
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
