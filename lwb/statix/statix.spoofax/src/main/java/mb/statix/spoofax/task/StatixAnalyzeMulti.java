package mb.statix.spoofax.task;

import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import mb.statix.StatixConstraintAnalyzer;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;

@mb.statix.spoofax.StatixScope
public class StatixAnalyzeMulti extends ConstraintAnalyzeMultiTaskDef {
    private final StatixConstraintAnalyzer constraintAnalyzer;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject
    public StatixAnalyzeMulti(StatixConstraintAnalyzer constraintAnalyzer, Provider<StrategoRuntime> strategoRuntimeProvider) {
        this.constraintAnalyzer = constraintAnalyzer;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override
    public String getId() {
        return "mb.statix.spoofax.task.GeneratedStatixAnalyzeMulti";
    }

    @Override
    protected ConstraintAnalyzer.MultiFileResult analyze(ResourcePath root, HashMap<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext context) throws ConstraintAnalyzerException {
        return constraintAnalyzer.analyze(root, asts, context, strategoRuntimeProvider.get().addContextObject(new Spoofax2ProjectContext(root)));
    }
}
