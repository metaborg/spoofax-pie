package mb.tiger.spoofax.taskdef.transform;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.transform.*;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.EnumSet;

public class TigerParsedAst implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerParse parse;

    @Inject public TigerParsedAst(TigerParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        if(!(input.subject instanceof FileSubject)) {
            throw new RuntimeException("Cannot get AST, subject '" + input.subject + "' is not a file subject");
        }
        final ResourceKey file = ((FileSubject) input.subject).getFile();
        final JSGLR1ParseResult parseOutput = context.require(parse, file);
        // TODO: if there is a region, select AST within the region.
//        final @Nullable Region region;
//        if(input.subject instanceof RegionSubject) {
//            region = ((RegionSubject) input.subject).getRegion();
//        } else {
//            region = null;
//        }
        if(parseOutput.ast == null) {
            throw new RuntimeException("Cannot get AST, parsed AST for '" + input.subject + "' is null");
        }
        final String formatted = StrategoUtil.toString(parseOutput.ast);
        return new TransformOutput(ListView.of(new OpenTextEditorFeedback(formatted)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show AST";
    }

    @Override public EnumSet<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSet.of(TransformExecutionType.OneShot, TransformExecutionType.Continuous);
    }

    @Override public EnumSet<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSet.of(TransformSubjectType.File, TransformSubjectType.FileRegion);
    }
}
