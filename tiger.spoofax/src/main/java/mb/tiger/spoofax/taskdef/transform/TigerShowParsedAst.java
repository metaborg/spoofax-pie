package mb.tiger.spoofax.taskdef.transform;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.transform.*;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowParsedAst implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerParse parse;


    @Inject public TigerShowParsedAst(TigerParse parse) {
        this.parse = parse;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        final TransformSubject subject = input.subject;
        final ResourceKey readable = TransformSubjects.getReadable(subject)
            .orElseThrow(() -> new RuntimeException("Cannot show parsed AST, subject '" + subject + "' is not a readable subject"));

        final JSGLR1ParseResult parseResult = context.require(parse, readable);
        final IStrategoTerm ast = parseResult.getAst()
            .orElseThrow(() -> new RuntimeException("Cannot show parsed AST, parsed AST for '" + readable + "' is null"));

        final IStrategoTerm term = TransformSubjects.caseOf(subject)
            .readableWithRegion((f, r) -> TermTracer.getSmallestTermEncompassingRegion(ast, r))
            .otherwise_(ast);

        final String formatted = StrategoUtil.toString(term);
        return new TransformOutput(ListView.of(TransformFeedbacks.openEditorWithText(formatted, "Parsed AST for '" + readable + "'", null)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show parsed AST";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.ManualOnce, TransformExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSetView.of(TransformSubjectType.Readable, TransformSubjectType.ReadableWithRegion);
    }
}
