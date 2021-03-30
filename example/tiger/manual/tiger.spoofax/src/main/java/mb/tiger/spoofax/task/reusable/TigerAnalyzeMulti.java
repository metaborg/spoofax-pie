package mb.tiger.spoofax.task.reusable;

import mb.common.util.MapView;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.common.StrategoRuntime;
import mb.tiger.TigerConstraintAnalyzer;
import mb.tiger.spoofax.TigerScope;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;

/**
 * @implNote Although Tiger is a single-file language, we implement the multi-file analysis variant here as well for
 * development/testing purposes.
 */
@TigerScope
public class TigerAnalyzeMulti extends ConstraintAnalyzeMultiTaskDef {
    private final ResourceService resourceService;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyzeMulti(ResourceService resourceService, Provider<StrategoRuntime> strategoRuntimeProvider, TigerConstraintAnalyzer constraintAnalyzer) {
        this.resourceService = resourceService;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override
    public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti";
    }

    @Override
    protected MultiFileResult analyze(ExecContext context, ResourcePath root, MapView<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext constraintAnalyzerContext) throws Exception {
        return constraintAnalyzer.analyze(root, asts, constraintAnalyzerContext, strategoRuntimeProvider.get(), resourceService);
    }
}
