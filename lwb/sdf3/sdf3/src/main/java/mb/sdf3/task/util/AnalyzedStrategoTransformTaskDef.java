package mb.sdf3.task.util;

import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.StrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class AnalyzedStrategoTransformTaskDef extends StrategoTransformTaskDef<ConstraintAnalyzeMultiTaskDef.SingleFileOutput> {
    public AnalyzedStrategoTransformTaskDef(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider, ListView<String> strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public AnalyzedStrategoTransformTaskDef(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider, String... strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    @Override
    protected StrategoRuntime getStrategoRuntime(ExecContext context, ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return super.getStrategoRuntime(context, input).addContextObject(input.context);
    }

    @Override
    protected IStrategoTerm getAst(ExecContext context, ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return input.result.ast;
    }
}
