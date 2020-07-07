package mb.tiger.spoofax.task.reusable;

import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerConstraintAnalyzer;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

@LanguageScope
public class TigerAnalyze extends ConstraintAnalyzeTaskDef {
    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyze(TigerConstraintAnalyzer constraintAnalyzer) {
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override
    public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeSingle";
    }

    @Override
    protected SingleFileResult analyze(ResourceKey resource, IStrategoTerm ast, ConstraintAnalyzerContext context) throws ConstraintAnalyzerException {
        return constraintAnalyzer.analyze(resource, ast, context);
    }
}
