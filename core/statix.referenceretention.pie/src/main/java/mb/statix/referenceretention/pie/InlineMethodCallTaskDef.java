package mb.statix.referenceretention.pie;

import mb.aterm.common.TermToString;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.jsglr.pie.JsglrParseTaskDef;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.statix.referenceretention.stratego.RRStrategoContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import mb.jsglr.common.TermTracer;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Task definition calling the Stratego transformation 'inline-method-call'
 * on an analyzed AST.
 */
public class InlineMethodCallTaskDef implements TaskDef<InlineMethodCallTaskDef.Input, CommandFeedback> {

    public static class Input implements Serializable {
//        /** The analyzed AST. */
//        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
//        public final Supplier<? extends Result<ConstraintAnalyzeTaskDef.Output, ?>> analysisSupplier;
        public final ResourceKey resource;
        public final @Nullable Region region;
//        /** The {@link SolverResult}, wrapped in a Stratego blob. */
//        public final IStrategoTerm soálverResultTerm;
//        public final ResourceKey resource;
//        public final ResourcePath rootDirectory;

        public Input(
//            Supplier<? extends Result<IStrategoTerm, ?>> astSupplier,
            ResourceKey resource,
//            Supplier<? extends Result<ConstraintAnalyzeTaskDef.Output, ?>> analysisSupplier,
            @Nullable Region region
//            IStrategoTerm solverResultTerm
//            ResourceKey resource,
//            ResourcePath rootDirectory
        ) {
//            this.astSupplier = astSupplier;
            this.resource = resource;
//            this.analysisSupplier = analysisSupplier;
            this.region = region;
//            this.solverResultTerm = solverResultTerm;
//            this.resource = resource;
//            this.rootDirectory = rootDirectory;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input that = (Input)o;
            return // astSupplier.equals(that.astSupplier)
                   this.resource.equals(that.resource)
//                   analysisSupplier.equals(that.analysisSupplier)
                && Objects.equals(this.region, that.region);
//                && solverResultTerm.equals(that.solverResultTerm);
//                && resource.equals(that.resource)
//                && rootDirectory.equals(that.rootDirectory);
        }

        @Override public int hashCode() {
            return Objects.hash(
//                astSupplier,
                resource,
//                analysisSupplier,
                region
//                solverResultTerm
//                resource,
//                rootDirectory
            );
        }

        @Override public String toString() {
            return "InlineMethodCallTaskDef.Input{" +
//                "astSupplier=" + astSupplier + "," +
                "file=" + resource + "," +
//                "analysisSupplier=" + analysisSupplier + "," +
                "region=" + region +
//                "solverResultTerm=" + solverResultTerm +
//                "file=" + resource + "," +
//                "rootDirectory=" + rootDirectory +
            '}';
        }
    }

//    private final JsglrParseTaskDef parseTaskDef;
//    private final ConstraintAnalyzeTaskDef analyzeTaskDef;
    private final JsglrParseTaskDef parseTaskDef;
    private final ConstraintAnalyzeTaskDef analyzeTaskDef;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final TegoRuntime tegoRuntime;
    private final StrategoTerms strategoTerms;
    private final Logger log;

    public InlineMethodCallTaskDef(
//        JsglrParseTaskDef parseTaskDef,
//        ConstraintAnalyzeTaskDef analyzeTaskDef,
        JsglrParseTaskDef parseTaskDef,
        ConstraintAnalyzeTaskDef analyzeTaskDef,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        TegoRuntime tegoRuntime,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory
    ) {
//        this.parseTaskDef = parseTaskDef;
//        this.analyzeTaskDef = analyzeTaskDef;
        this.parseTaskDef = parseTaskDef;
        this.analyzeTaskDef = analyzeTaskDef;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.tegoRuntime = tegoRuntime;
        this.strategoTerms = strategoTerms;
        this.log = loggerFactory.create(getClass());
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Input input) throws Exception {

        final ResourceKey key = input.resource;
        final Supplier<Result<ConstraintAnalyzeTaskDef.Output, ?>> analysisSupplier = analyzeTaskDef
            .createSupplier(new ConstraintAnalyzeTaskDef.Input(key, parseTaskDef.inputBuilder().withFile(key).buildAstSupplier()));

        final RRStrategoContext rrctx = new RRStrategoContext(
            tegoRuntime,
            strategoTerms,
            "qualify-reference" // TODO: Make this configurable?
        );
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(rrctx);
        rrctx.strategoRuntime = strategoRuntime;

        return context.require(analysisSupplier)
            .flatMapOrElse((analysis) -> {
                try {
                    log.info("Calling inline-method-call...");
                    final IStrategoTerm ast = analysis.result.analyzedAst;
                    final IStrategoTerm regionAst;
                    if(input.region != null) {
                        regionAst = TermTracer.getSmallestTermEncompassingRegion(ast, input.region);
                    } else {
                        regionAst = ast;
                    }
                    final IStrategoTerm solverResultTerm = analysis.result.analysis;
                    // <inline-method-call(|"test", solverResultTerm)> regionAst
                    final IStrategoTerm newAst = strategoRuntime.invoke(
                        "inline-method-call",
                        regionAst,
                        strategoRuntime.getTermFactory().makeString("test"),
                        solverResultTerm
                    );
                    log.info("Called inline-method-call");
                    return Result.ofOk(newAst);
                } catch(StrategoException e) {
                    return Result.ofErr(e);
                }
            }, Result::ofErr)
            .map(TermToString::toString)
            .mapOrElse(
                text -> CommandFeedback.of(ShowFeedback.showText(text, "Inlined method call in '" + key + "'")),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, key));

//
//        final Result<ConstraintAnalyzeTaskDef.Output, ?> analysisResult = context.require(
//            analyzeTask,
//            new ConstraintAnalyzeTaskDef.Input(input.resource, input.astSupplier)
////            new ConstraintAnalyzeTaskDef.Input(input.rootDirectory, input.file, input.astSupplier)
//        );
//
//        if(!analysisResult.isOk()) {
//            return analysisResult.ignoreValueIfErr();
//        }
//        // This is a SolverResult wrapped as a Blob Stratego term.
//        final IStrategoTerm solverResultTerm = analysisResult.get().result.analysis;
//        // This is the analyzed AST
//        final IStrategoTerm ast = analysisResult.get().result.analyzedAst;

//        try {
//            log.info("Calling inline-method-call...");
//            // TODO: Call inline-method-call with an analysis result
//            IStrategoTerm newAst = strategoRuntime.invoke("inline-method-call", ast, strategoRuntime.getTermFactory().makeString("test"));
//            log.info("Called inline-method-call");
//            return Result.ofOk(newAst);
//        } catch(StrategoException e) {
//            return Result.ofErr(e);
//        }
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

}
