package mb.statix.task;

import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import mb.statix.StatixConstraintAnalyzer;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoTerm;
import mb.statix.StatixScope;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;

@StatixScope
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
        return "mb.statix.task.GeneratedStatixAnalyzeMulti";
    }

    @Override
    protected ConstraintAnalyzer.MultiFileResult analyze(ExecContext context, ResourcePath root, HashMap<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext constraintAnalyzerContext) throws ConstraintAnalyzerException {
        return constraintAnalyzer.analyze(root, asts, constraintAnalyzerContext, strategoRuntimeProvider.get().addContextObject(new Spoofax2ProjectContext(root)));
    }
}
