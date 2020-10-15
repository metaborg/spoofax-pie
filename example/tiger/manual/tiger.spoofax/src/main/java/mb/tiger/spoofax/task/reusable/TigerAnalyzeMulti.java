package mb.tiger.spoofax.task.reusable;

import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.resource.ResourceKey;
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
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyzeMulti(Provider<StrategoRuntime> strategoRuntimeProvider, TigerConstraintAnalyzer constraintAnalyzer) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override
    public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti";
    }

    @Override
    protected MultiFileResult analyze(ResourcePath root, HashMap<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext context) throws ConstraintAnalyzerException {
        return constraintAnalyzer.analyze(root, asts, context, strategoRuntimeProvider.get());
    }
}
