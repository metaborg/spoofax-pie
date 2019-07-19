package mb.tiger.spoofax.taskdef.transform;

import mb.common.region.Region;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.*;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowDesugaredAst implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerParse parse;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;


    @Inject public TigerShowDesugaredAst(
        TigerParse parse,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime
    ) {
        this.parse = parse;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        if(!(input.subject instanceof FileSubject)) {
            throw new RuntimeException("Cannot show desugared AST, subject '" + input.subject + "' is not a file subject");
        }
        final ResourcePath file = ((FileSubject) input.subject).getFile();

        final JSGLR1ParseResult parseOutput = context.require(parse, file);
        if(parseOutput.ast == null) {
            throw new RuntimeException("Cannot show desugared AST, parsed AST for '" + input.subject + "' is null");
        }

        final IStrategoTerm term;
        if(input.subject instanceof RegionSubject) {
            final Region region = ((RegionSubject) input.subject).getRegion();
            term = TermTracer.getSmallestTermEncompassingRegion(parseOutput.ast, region);
        } else {
            term = parseOutput.ast;
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "desugar-all";
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, term, new IOAgent());
        if(result == null) {
            throw new RuntimeException("Cannot show desugared AST, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new TransformOutput(ListView.of(new OpenTextEditorFeedback(formatted, "Desugared AST for '" + file + "'")));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show desugared AST";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.OneShot, TransformExecutionType.ContinuousOnEditor);
    }

    @Override public EnumSetView<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSetView.of(TransformSubjectType.File, TransformSubjectType.FileRegion);
    }
}
