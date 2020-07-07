package mb.tiger.spoofax.task.reusable;

import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerConstraintAnalyzer;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * @implNote Although Tiger is a single-file language, we implement the multi-file analysis variant here as well for
 * development/testing purposes.
 */
@LanguageScope
public class TigerAnalyzeMulti extends ConstraintAnalyzeMultiTaskDef {
    private final TigerConstraintAnalyzer constraintAnalyzer;

    @Inject
    public TigerAnalyzeMulti(TigerConstraintAnalyzer constraintAnalyzer) {
        this.constraintAnalyzer = constraintAnalyzer;
    }

    @Override
    public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti";
    }

    @Override
    protected MultiFileResult analyze(ResourceKey root, HashMap<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext context) throws ConstraintAnalyzerException {
        return constraintAnalyzer.analyze(root, asts, context);
    }
}
