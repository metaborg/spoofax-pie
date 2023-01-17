package mb.statix.referenceretention.pie;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.referenceretention.stratego.RRStrategoContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Task definition calling the Stratego transformation 'inline-method-call'
 * on an analyzed AST.
 */
public class InlineMethodCallTaskDef implements TaskDef<InlineMethodCallTaskDef.Input, Result<IStrategoTerm, ?>> {

    public static class Input implements Serializable {
        public final Supplier<? extends Result<IStrategoTerm, ?>> astSupplier;
        public final ResourceKey file;
        public final ResourcePath rootDirectory;

        public Input(
            Supplier<? extends Result<IStrategoTerm, ?>> astSupplier,
            ResourceKey file,
            ResourcePath rootDirectory
        ) {
            this.astSupplier = astSupplier;
            this.file = file;
            this.rootDirectory = rootDirectory;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return astSupplier.equals(input.astSupplier)
                && file.equals(input.file)
                && rootDirectory.equals(input.rootDirectory);
        }

        @Override public int hashCode() {
            return Objects.hash(
                astSupplier,
                file,
                rootDirectory
            );
        }

        @Override public String toString() {
            return "InlineMethodCallTaskDef.Input{" +
                "astSupplier=" + astSupplier + "," +
                "file=" + file + "," +
                "rootDirectory=" + rootDirectory +
            '}';
        }
    }

    private final Provider<StrategoRuntime> strategoRuntimeProvider;
    private final ConstraintAnalyzeFile analyzeFile;    // TODO: Should be TigerAnalyzeFile or something
    private final TegoRuntime tegoRuntime;
    private final StrategoTerms strategoTerms;
    private final Logger log;

    @Inject public InlineMethodCallTaskDef(
        Provider<StrategoRuntime> strategoRuntimeProvider,
        ConstraintAnalyzeFile analyzeFile,
        TegoRuntime tegoRuntime,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory
    ) {
        this.strategoRuntimeProvider = strategoRuntimeProvider;
        this.analyzeFile = analyzeFile;
        this.tegoRuntime = tegoRuntime;
        this.strategoTerms = strategoTerms;
        this.log = loggerFactory.create(getClass());
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, Input input) throws Exception {
        final RRStrategoContext rrctx = new RRStrategoContext(
            tegoRuntime,
            strategoTerms,
            "qualify-reference" // TODO: Make this configurable?
        );
        final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(rrctx);
        rrctx.strategoRuntime = strategoRuntime;

        final Result<ConstraintAnalyzeFile.Output, ?> analysisResult = context.require(
            analyzeFile,
            new ConstraintAnalyzeFile.Input(input.rootDirectory, input.file)
        );

        if(!analysisResult.isOk()) {
            return analysisResult.ignoreValueIfErr();
        }
        // This is a SolverResult wrapped as a Blob Stratego term.
        final IStrategoTerm solverResultTerm = analysisResult.get().analysis;
        // This is the analyzed AST
        final IStrategoTerm ast = analysisResult.get().ast;

        try {
            log.info("Calling inline-method-call...");
            // TODO: Call inline-method-call with an analysis result
            IStrategoTerm newAst = strategoRuntime.invoke("inline-method-call", ast, strategoRuntime.getTermFactory().makeString("test"));
            log.info("Called inline-method-call");
            return Result.ofOk(newAst);
        } catch(StrategoException e) {
            return Result.ofErr(e);
        }
//
//        return context.require(input.astSupplier)
//            .flatMapOrElse((ast) -> {
//                try {
//                    log.info("Calling inline-method-call...");
//                    // TODO: Call inline-method-call with an analysis result
//                    ast = strategoRuntime.invoke("inline-method-call", ast, strategoRuntime.getTermFactory().makeString("test"));
//                    log.info("Called inline-method-call");
//                    return Result.ofOk(ast);
//                } catch(StrategoException e) {
//                    return Result.ofErr(e);
//                }
//            }, Result::ofErr);
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

}
