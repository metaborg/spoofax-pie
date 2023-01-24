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
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.statix.referenceretention.stratego.RRStrategoContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import mb.jsglr.common.TermTracer;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Task definition calling the Stratego transformation 'inline-method-call'
 * on an analyzed AST.
 */
public class InlineMethodCallTaskDef implements TaskDef<InlineMethodCallTaskDef.Args, CommandFeedback> {

    public static class Args implements Serializable {
        public final ResourcePath project;
        public final ResourceKey resource;
        public final @Nullable Region region;

        public Args(
            ResourcePath project,
            ResourceKey resource,
            @Nullable Region region
        ) {
            this.project = project;
            this.resource = resource;
            this.region = region;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args that = (Args)o;
            return Objects.equals(this.project, that.project)
                && this.resource.equals(that.resource)
                && Objects.equals(this.region, that.region);
        }

        @Override public int hashCode() {
            return Objects.hash(
                project,
                resource,
                region
            );
        }

        @Override public String toString() {
            return "InlineMethodCallTaskDef.Input{" +
                "project=" + project + "," +
                "resource=" + resource + "," +
                "region=" + region +
            '}';
        }
    }

    private final JsglrParseTaskDef parseTaskDef;
    private final ConstraintAnalyzeTaskDef analyzeTaskDef;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final TegoRuntime tegoRuntime;
    private final StrategoTerms strategoTerms;
    private final Logger log;

    public InlineMethodCallTaskDef(
        JsglrParseTaskDef parseTaskDef,
        ConstraintAnalyzeTaskDef analyzeTaskDef,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        TegoRuntime tegoRuntime,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory
    ) {
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

    @Override public CommandFeedback exec(ExecContext context, Args input) throws Exception {

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
                        ast,
                        regionAst,
                        //strategoRuntime.getTermFactory().makeString("test"),
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
    }

    @Override public boolean shouldExecWhenAffected(Args input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

}
