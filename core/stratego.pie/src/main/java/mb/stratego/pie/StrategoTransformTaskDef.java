package mb.stratego.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.OutTransient;
import mb.pie.api.Supplier;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

/**
 * Abstract task definition that gets the Stratego runtime provider with the provided implementation of {@link
 * GetStrategoRuntimeProvider}, gets a Stratego runtime, and then executes in sequence the provided strategies on the
 * supplied {@link IStrategoTerm AST} extracted from the {@link T input}.
 *
 * Inputs of this task are of type {@code Supplier<? extends Result<T, ?>>} such that {@link IStrategoTerm ASTs} can be
 * {@link Supplier incrementally supplied}, and support {@link Result failure}.
 *
 * Outputs are of type {@code Result<IStrategoTerm, ?>}.
 *
 * This class should be implemented by language developers to create task definitions that run Stratego strategies on
 * {@link IStrategoTerm ASTs} extracted from the {@link T input} by:
 *
 * <ul>
 * <li>Providing the {@link GetStrategoRuntimeProvider} task definition in the constructor</li>
 * <li>Providing the strategies to execute in the constructor</li>
 * <li>Overriding {@link #getAst(ExecContext, T)} to extract the {@link IStrategoTerm AST}</li>
 * <li>Overriding {@link #getId()} to give the task definition a unique ID</li>
 * <li>Overriding {@link #createDependencies(ExecContext)} to create a self-dependency to your class</li>
 * </ul>
 *
 * @param <T> Type of wrapped inputs to this task definition. The actual input is {@code Supplier<Result<T, ?>>}.
 */
public abstract class StrategoTransformTaskDef<T> extends BaseStrategoTransformTaskDef<T> {
    private final GetStrategoRuntimeProvider getStrategoRuntimeProvider;

    public StrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategyNames);
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    public StrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, String... strategyNames) {
        super(strategyNames);
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override protected StrategoRuntime getStrategoRuntime(ExecContext context, T input) {
        final OutTransient<Provider<StrategoRuntime>> provider = context.require(getStrategoRuntimeProvider, None.instance);
        return provider.getValue().get();
    }
}
