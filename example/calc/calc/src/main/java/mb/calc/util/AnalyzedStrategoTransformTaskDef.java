package mb.calc.util;

import mb.calc.task.CalcGetStrategoRuntimeProvider;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.pie.api.ExecContext;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.StrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class AnalyzedStrategoTransformTaskDef extends StrategoTransformTaskDef<ConstraintAnalyzeTaskDef.Output> {
    public AnalyzedStrategoTransformTaskDef(CalcGetStrategoRuntimeProvider getStrategoRuntimeProvider, ListView<String> strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    public AnalyzedStrategoTransformTaskDef(CalcGetStrategoRuntimeProvider getStrategoRuntimeProvider, String... strategyNames) {
        super(getStrategoRuntimeProvider, strategyNames);
    }

    @Override protected StrategoRuntime getStrategoRuntime(ExecContext context, ConstraintAnalyzeTaskDef.Output input) {
        return super.getStrategoRuntime(context, input).addContextObject(input.context);
    }

    @Override protected IStrategoTerm getAst(ExecContext context, ConstraintAnalyzeTaskDef.Output input) {
        return input.result.ast;
    }
}
