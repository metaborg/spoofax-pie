package mb.calc.spoofax.util;

import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.ProviderStrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class AnalyzedStrategoTransformTaskDef extends ProviderStrategoTransformTaskDef<ConstraintAnalyzeTaskDef.Output> {
    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategoRuntimeProvider, strategyNames);
    }

    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        super(strategoRuntimeProvider, strategyNames);
    }

    @Override protected StrategoRuntime getStrategoRuntime(ConstraintAnalyzeTaskDef.Output input) {
        return super.getStrategoRuntime(input).addContextObject(input.context);
    }

    @Override protected IStrategoTerm getAst(ConstraintAnalyzeTaskDef.Output input) {
        return input.result.ast;
    }
}
