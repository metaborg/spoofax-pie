package mb.tiger.spoofax.task.reusable;

import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoRuntime;
import mb.tiger.TigerConstraintAnalyzer;
import mb.tiger.spoofax.TigerScope;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

@TigerScope
public class TigerAnalyze extends ConstraintAnalyzeTaskDef {
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyze(Provider<StrategoRuntime> strategoRuntimeProvider, TigerConstraintAnalyzer constraintAnalyzer) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override
    public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeSingle";
    }

    @Override
    protected SingleFileResult analyze(ResourceKey resource, IStrategoTerm ast, ConstraintAnalyzerContext context) throws ConstraintAnalyzerException {
        return constraintAnalyzer.analyze(resource, ast, context, strategoRuntimeProvider.get());
    }
}
