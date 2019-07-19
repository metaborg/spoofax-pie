package mb.tiger.spoofax.taskdef.transform;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.*;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerAnalyze;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowScopeGraph implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerParse parse;
    private final TigerAnalyze analyze;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;


    @Inject
    public TigerShowScopeGraph(TigerParse parse, TigerAnalyze analyze, StrategoRuntimeBuilder strategoRuntimeBuilder, StrategoRuntime prototypeStrategoRuntime) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        if(!(input.subject instanceof FileSubject)) {
            throw new RuntimeException("Cannot show scope graph, subject '" + input.subject + "' is not a file subject");
        }
        final ResourcePath file = ((FileSubject) input.subject).getFile();

        final JSGLR1ParseResult parseResult = context.require(parse, file);
        if(parseResult.ast == null) {
            throw new RuntimeException("Cannot show scope graph, parsed AST for '" + input.subject + "' is null");
        }

        final ConstraintAnalyzer.@Nullable SingleFileResult analysisResult = context.require(analyze, file);
        if(analysisResult == null) {
            throw new RuntimeException("Cannot show scope graph, analysis result for '" + input.subject + "' is null");
        }
        if(analysisResult.ast == null) {
            throw new RuntimeException("Cannot show scope graph, analyzed AST for '" + input.subject + "' is null");
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "editor-show-analysis-term";
        final IStrategoTerm term = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), analysisResult.ast, file);
        final @Nullable IStrategoTerm result = strategoRuntime.invoke(strategyId, term, new IOAgent());
        if(result == null) {
            throw new RuntimeException("Cannot show scope graph, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new TransformOutput(ListView.of(new OpenTextEditorFeedback(formatted, "Scope graph for '" + file + "'")));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show scope graph";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.OneShot, TransformExecutionType.ContinuousOnEditor);
    }

    @Override public EnumSetView<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSetView.of(TransformSubjectType.File);
    }
}
