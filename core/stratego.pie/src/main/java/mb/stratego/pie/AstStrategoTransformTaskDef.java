package mb.stratego.pie;

import mb.common.util.ListView;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class AstStrategoTransformTaskDef extends ProviderStrategoTransformTaskDef<IStrategoTerm> {
    public AstStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategoRuntimeProvider, strategyNames);
    }

    public AstStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        super(strategoRuntimeProvider, strategyNames);
    }

    @Override protected IStrategoTerm getAst(IStrategoTerm input) {
        return input;
    }
}
