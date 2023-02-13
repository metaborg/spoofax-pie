package mb.stratego.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Abstract base task definition class that executes a sequence of Stratego strategies. The Stratego runtime to execute
 * with is provided by {@link #getStrategoRuntime(ExecContext, Object)}, and the initial AST to apply it to with {@link
 * #getAst(ExecContext, Object)}. The inputs of this task are of type {@code Supplier<? extends Result<T, ?>>} such that
 * {@link IStrategoTerm ASTs} can be {@link Supplier incrementally supplied}, and support {@link Result failure}.
 *
 * This task definition is normally used through {@link StrategoTransformTaskDef}.
 *
 * @param <T> Type of wrapped inputs to this task definition, from which an {@link IStrategoTerm AST} can be extracted.
 */
public abstract class BaseStrategoTransformTaskDef<T> implements TaskDef<Supplier<? extends Result<T, ?>>, Result<IStrategoTerm, ?>> {

    private final ListView<Strategy> strategies;

    public BaseStrategoTransformTaskDef(ListView<String> strategyNames) {
        this.strategies = ListView.of(
            strategyNames.stream()
            .map(Strategy::strategy)
            .collect(Collectors.toList())
        );
    }

    public BaseStrategoTransformTaskDef(String... strategyNames) {
        this.strategies = ListView.of(
            Arrays.stream(strategyNames)
                .map(Strategy::strategy)
                .collect(Collectors.toList())
        );
    }

    public BaseStrategoTransformTaskDef(Strategy... strategies) {
        this.strategies = ListView.of(strategies);
    }

    protected void createDependencies(ExecContext context) throws Exception {}

    protected abstract StrategoRuntime getStrategoRuntime(ExecContext context, T input);

    protected abstract IStrategoTerm getAst(ExecContext context, T input);

    protected ListView<Strategy> getStrategies(ExecContext context, T input) { return strategies; }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, Supplier<? extends Result<T, ?>> supplier) throws Exception {
        createDependencies(context);
        return context.require(supplier).flatMapOrElse((t) -> {
            final StrategoRuntime strategoRuntime = getStrategoRuntime(context, t);
            IStrategoTerm ast = getAst(context, t);
            for(Strategy strategy : getStrategies(context, t)) {
                try {
                    ast = strategoRuntime.invoke(strategy, ast);
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }
            return Result.ofOk(ast);
        }, Result::ofErr);
    }
}
