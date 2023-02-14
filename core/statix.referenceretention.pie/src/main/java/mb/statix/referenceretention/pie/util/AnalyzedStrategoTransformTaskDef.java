package mb.statix.referenceretention.pie.util;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.Strategy;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.stratego.pie.StrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class AnalyzedStrategoTransformTaskDef extends StrategoTransformTaskDef<ConstraintAnalyzeMultiTaskDef.SingleFileOutput> {
    public AnalyzedStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, ListView<String> strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public AnalyzedStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, String... strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public AnalyzedStrategoTransformTaskDef(GetStrategoRuntimeProvider getStrategoRuntimeProvider, Strategy... strategies) {
        super(getStrategoRuntimeProvider, strategies);
    }

    @Override
    protected StrategoRuntime getStrategoRuntime(ExecContext context, ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return super.getStrategoRuntime(context, input).addContextObject(input.context);
    }

    @Override
    protected Result<IStrategoTerm, ?> getAst(ExecContext context, ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return Result.ofOk(input.result.analyzedAst);
    }
}
