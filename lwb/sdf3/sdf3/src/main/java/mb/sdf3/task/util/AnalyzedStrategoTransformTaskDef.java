package mb.sdf3.task.util;

import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.ProviderStrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class AnalyzedStrategoTransformTaskDef extends ProviderStrategoTransformTaskDef<ConstraintAnalyzeMultiTaskDef.SingleFileOutput> {
    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategoRuntimeProvider, strategyNames);
    }

    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        super(strategoRuntimeProvider, strategyNames);
    }

    @Override protected StrategoRuntime getStrategoRuntime(ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return super.getStrategoRuntime(input).addContextObject(input.context);
    }

    @Override protected IStrategoTerm getAst(ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return input.result.ast;
    }
}
