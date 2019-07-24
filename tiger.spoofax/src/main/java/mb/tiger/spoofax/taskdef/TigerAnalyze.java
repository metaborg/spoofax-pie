package mb.tiger.spoofax.taskdef;

import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoIOAgent;
import mb.tiger.TigerConstraintAnalyzer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.Optional;

public class TigerAnalyze implements TaskDef<ResourceKey, @Nullable SingleFileResult> {
    private final TigerParse parse;
    private final TigerConstraintAnalyzer constraintAnalyzer;
    private final LoggerFactory loggerFactory;
    private final ResourceService resourceService;

    @Inject
    public TigerAnalyze(TigerParse parse, TigerConstraintAnalyzer constraintAnalyzer, LoggerFactory loggerFactory, ResourceService resourceService) {
        this.parse = parse;
        this.constraintAnalyzer = constraintAnalyzer;
        this.loggerFactory = loggerFactory;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable SingleFileResult exec(ExecContext context, ResourceKey key) throws Exception {
        final JSGLR1ParseResult parseResult = context.require(parse, key);
        final Optional<IStrategoTerm> ast = parseResult.getAst();
        if(!ast.isPresent()) {
            return null;
        }

        try {
            return constraintAnalyzer.analyze(key, ast.get(), new ConstraintAnalyzerContext(), new StrategoIOAgent(loggerFactory, resourceService));
        } catch(ConstraintAnalyzerException e) {
            throw new RuntimeException("Constraint analysis failed unexpectedly", e);
        }
    }
}
