package mb.stratego.pie;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.OutTransient;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

/**
 * Abstract task definition that gets the Stratego runtime provider with the provided implementation of {@link
 * GetStrategoRuntimeProvider}, gets a Stratego runtime, and then executes the provided strategies on the {@link
 * IStrategoTerm AST} extracted from the {@link T input} in sequence.
 *
 * This class should be implemented by language developers to create task definitions that run Stratego strategies on
 * {@link IStrategoTerm ASTs} extracted from the {@link T input} by providing the {@link GetStrategoRuntimeProvider} and
 * strategies to execute in the constructor, by overriding {@link #getAst(ExecContext, T)} to extract the {@link
 * IStrategoTerm AST}, and by overriding {@link #getId()}
 *
 * @param <T> Type of inputs to this task definition.
 */
public abstract class ProviderStrategoTransformTaskDef<T> extends StrategoTransformTaskDef<T> {
    private final GetStrategoRuntimeProvider getStrategoRuntimeProvider;

    public ProviderStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategyNames);
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    public ProviderStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, String... strategyNames) {
        super(strategyNames);
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override protected StrategoRuntime getStrategoRuntime(ExecContext context, T input) {
        final OutTransient<Provider<StrategoRuntime>> provider = context.require(getStrategoRuntimeProvider, None.instance);
        return provider.getValue().get();
    }
}
