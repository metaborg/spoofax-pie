package mb.tiger.spoofax.task.reusable;

import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoRuntime;
import mb.tiger.TigerConstraintAnalyzer;
import mb.tiger.spoofax.TigerScope;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;

@TigerScope
public class TigerAnalyze extends ConstraintAnalyzeTaskDef {
    private final ResourceService resourceService;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyze(ResourceService resourceService, Provider<StrategoRuntime> strategoRuntimeProvider, TigerConstraintAnalyzer constraintAnalyzer) {
        this.resourceService = resourceService;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override
    public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeSingle";
    }

    @Override
    protected SingleFileResult analyze(ExecContext context, ResourceKey resource, IStrategoTerm ast, ConstraintAnalyzerContext constraintAnalyzerContext) throws Exception {
        return constraintAnalyzer.analyze(resource, ast, constraintAnalyzerContext, strategoRuntimeProvider.get(), resourceService);
    }
}
