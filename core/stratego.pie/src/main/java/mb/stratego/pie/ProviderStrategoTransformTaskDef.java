package mb.stratego.pie;

import mb.common.util.ListView;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Provider;

public abstract class ProviderStrategoTransformTaskDef<T> extends StrategoTransformTaskDef<T> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    public ProviderStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategyNames);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    public ProviderStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        super(strategyNames);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override protected StrategoRuntime getStrategoRuntime(T input) {
        return strategoRuntimeProvider.get();
    }
}
