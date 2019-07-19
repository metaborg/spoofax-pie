package mb.tiger.spoofax.taskdef.transform;

import mb.common.region.Region;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.*;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerAnalyze;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowAnalyzedAst implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerParse parse;
    private final TigerAnalyze analyze;


    @Inject
    public TigerShowAnalyzedAst(TigerParse parse, TigerAnalyze analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        if(!(input.subject instanceof FileSubject)) {
            throw new RuntimeException("Cannot show analyzed AST, subject '" + input.subject + "' is not a file subject");
        }
        final ResourcePath file = ((FileSubject) input.subject).getFile();

        final JSGLR1ParseResult parseResult = context.require(parse, file);
        if(parseResult.ast == null) {
            throw new RuntimeException("Cannot show analyzed AST, parsed AST for '" + input.subject + "' is null");
        }

        final ConstraintAnalyzer.@Nullable SingleFileResult analysisResult = context.require(analyze, file);
        if(analysisResult == null) {
            throw new RuntimeException("Cannot show analyzed AST, analysis result for '" + input.subject + "' is null");
        }
        if(analysisResult.ast == null) {
            throw new RuntimeException("Cannot show analyzed AST, analyzed AST for '" + input.subject + "' is null");
        }

        final IStrategoTerm term;
        if(input.subject instanceof RegionSubject) {
            final Region region = ((RegionSubject) input.subject).getRegion();
            term = TermTracer.getSmallestTermEncompassingRegion(analysisResult.ast, region);
        } else {
            term = analysisResult.ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new TransformOutput(ListView.of(new OpenTextEditorFeedback(formatted, "Analyzed AST for '" + file + "'")));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show analyzed AST";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.OneShot, TransformExecutionType.ContinuousOnEditor);
    }

    @Override public EnumSetView<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSetView.of(TransformSubjectType.File, TransformSubjectType.FileRegion);
    }
}
