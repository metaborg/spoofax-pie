package mb.tiger.spoofax.taskdef.transform;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.*;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.util.EnumSet;

public class TigerShowParsedAst implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerParse parse;


    @Inject public TigerShowParsedAst(TigerParse parse) {
        this.parse = parse;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        if(!(input.subject instanceof FileSubject)) {
            throw new RuntimeException("Cannot show parsed AST, subject '" + input.subject + "' is not a file subject");
        }
        final ResourcePath file = ((FileSubject) input.subject).getFile();

        final JSGLR1ParseResult parseOutput = context.require(parse, file);
        if(parseOutput.ast == null) {
            throw new RuntimeException("Cannot show parsed AST, parsed AST for '" + input.subject + "' is null");
        }

        final IStrategoTerm term;
        if(input.subject instanceof RegionSubject) {
            final Region region = ((RegionSubject) input.subject).getRegion();
            term = TermTracer.getSmallestTermEncompassingRegion(parseOutput.ast, region);
        } else {
            term = parseOutput.ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new TransformOutput(ListView.of(new OpenTextEditorFeedback(formatted)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show parsed AST";
    }

    @Override public EnumSet<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSet.of(TransformExecutionType.OneShot, TransformExecutionType.Continuous);
    }

    @Override public EnumSet<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSet.of(TransformSubjectType.File, TransformSubjectType.FileRegion);
    }
}
