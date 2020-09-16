package mb.sdf3.spoofax.task.util;

import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.StrategoTransformTaskDef;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Provider;

public abstract class AnalyzedStrategoTransformTaskDef extends StrategoTransformTaskDef<ConstraintAnalyzeMultiTaskDef.SingleFileOutput> {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, ListView<String> strategyNames) {
        super(strategyNames);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String... strategyNames) {
        super(strategyNames);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    public AnalyzedStrategoTransformTaskDef(Provider<StrategoRuntime> strategoRuntimeProvider, String strategyName) {
        super(strategyName);
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override protected StrategoRuntime getStrategoRuntime(ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return strategoRuntimeProvider.get().addContextObject(input.context);
    }

    @Override protected IStrategoTerm getAst(ConstraintAnalyzeMultiTaskDef.SingleFileOutput input) {
        return input.result.ast;
    }
}
