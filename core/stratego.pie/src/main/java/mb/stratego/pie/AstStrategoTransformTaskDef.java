package mb.stratego.pie;

import mb.common.util.ListView;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class AstStrategoTransformTaskDef extends StrategoTransformTaskDef<IStrategoTerm> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    public AstStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategyNames);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    public AstStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        super(strategyNames);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    public AstStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String strategyName) {
        super(strategyName);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override protected StrategoRuntime getStrategoRuntime(IStrategoTerm input) {
        return strategoRuntimeProvider.get();
    }

    @Override protected IStrategoTerm getAst(IStrategoTerm input) {
        return input;
    }
}
