package mb.tiger.spoofax.taskdef;

import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.AstResult;
import mb.stratego.common.StrategoIOAgent;
import mb.tiger.TigerConstraintAnalyzer;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class TigerAnalyze implements TaskDef<ResourceKey, @Nullable SingleFileResult> {
    private final TigerGetAST getAst;
    private final TigerConstraintAnalyzer constraintAnalyzer;
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;

    @Inject
    public TigerAnalyze(TigerGetAST getAst, TigerConstraintAnalyzer constraintAnalyzer, LoggerFactory loggerFactory, ResourceService resourceService) {
        this.getAst = getAst;
        this.constraintAnalyzer = constraintAnalyzer;
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable SingleFileResult exec(ExecContext context, ResourceKey key) throws Exception {
        final AstResult result = context.require(getAst, key);
        if(result.ast == null) {
            return null;
        }
        try {
            return constraintAnalyzer.analyze(key, result.ast, new ConstraintAnalyzerContext(),
                new StrategoIOAgent(loggerFactory, resourceService));
        } catch(ConstraintAnalyzerException e) {
            throw new RuntimeException("Constraint analysis failed unexpectedly", e);
        }
    }
}
