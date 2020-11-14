package mb.statix.multilang.pie;

import dagger.Lazy;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.statix.constraints.CUser;
import mb.statix.multilang.metadata.FileResult;
import mb.statix.multilang.metadata.ImmutableFileResult;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadataManager;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@MultiLangScope
public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, Result<FileResult, MultiLangAnalysisException>> {
    public static class Input implements Serializable {
        private final LanguageId languageId;
        private final ResourceKey resourceKey;

        private final Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier;

        private final @Nullable Level logLevel;

        public Input(
            LanguageId languageId,
            ResourceKey resourceKey,
            Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier,
            @Nullable Level logLevel
        ) {
            this.languageId = languageId;
            this.resourceKey = resourceKey;
            this.globalResultSupplier = globalResultSupplier;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return Objects.equals(languageId, input.languageId) &&
                Objects.equals(resourceKey, input.resourceKey) &&
                Objects.equals(globalResultSupplier, input.globalResultSupplier);
        }

        @Override public int hashCode() {
            return Objects.hash(languageId, resourceKey, globalResultSupplier);
        }

        @Override public String toString() {
            return "Input{" +
                "languageId=" + languageId +
                ", resourceKey=" + resourceKey +
                ", globalResultSupplier=" + globalResultSupplier +
                '}';
        }
    }

    private final Lazy<LanguageMetadataManager> languageMetadataManager;
    private final SmlBuildSpec buildSpec;
    private final Logger logger;

    @Inject public SmlPartialSolveFile(
        @MultiLang Lazy<LanguageMetadataManager> languageMetadataManager,
        SmlBuildSpec buildSpec,
        LoggerFactory loggerFactory
    ) {
        this.languageMetadataManager = languageMetadataManager;
        this.buildSpec = buildSpec;
        this.logger = loggerFactory.create(SmlPartialSolveFile.class);
    }

    @Override public String getId() {
        return SmlPartialSolveFile.class.getCanonicalName();
    }

    @Override public Result<FileResult, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        return languageMetadataManager.get().getLanguageMetadataResult(input.languageId).flatMap(languageMetadata -> {
            Supplier<Result<IStrategoTerm, ?>> astSupplier = languageMetadata.astFunction().createSupplier(input.resourceKey);
            return TaskUtils.executeWrapped(() -> context.require(astSupplier)
                    .mapErr(err -> MultiLangAnalysisException.wrapIfNeeded("No ast provided for " + input.resourceKey, err))
                    .flatMap((IStrategoTerm ast) -> analyzeAst(context, input, ast)),
                "Error loading file AST");
        });
    }

    private Result<FileResult, MultiLangAnalysisException> analyzeAst(ExecContext context, Input input, IStrategoTerm ast) {
        return TaskUtils.executeWrapped(() -> context.require(input.globalResultSupplier)
                .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                .flatMap(globalResult -> analyzeForGlobal(context, input, ast, globalResult)),
            "Exception when resolving global result");
    }

    private Result<FileResult, MultiLangAnalysisException> analyzeForGlobal(ExecContext context, Input input, IStrategoTerm ast, GlobalResult globalResult) {
        return TaskUtils.executeWrapped(() -> context.require(buildSpec.createSupplier(new SmlBuildSpec.Input(input.languageId)))
            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
            .flatMap(spec -> languageMetadataManager.get().getLanguageMetadataResult(input.languageId).flatMap(languageMetadata -> {
                StrategoTerms st = new StrategoTerms(languageMetadata.termFactory());

                IDebugContext debug = TaskUtils.createDebugContext(input.logLevel);
                Iterable<ITerm> constraintArgs = Arrays.asList(globalResult.globalScope(), st.fromStratego(ast));
                IConstraint fileConstraint = new CUser(languageMetadata.fileConstraint(), constraintArgs, null);

                return TaskUtils.executeWrapped(() -> {
                    long t0 = System.currentTimeMillis();
                    SolverResult result = SolverUtils.partialSolve(spec,
                        State.of(spec)
                            .add(globalResult.result().state())
                            .withResource(input.resourceKey.toString()),
                        fileConstraint,
                        debug,
                        new NullProgress(),
                        new NullCancel()
                    );
                    long dt = System.currentTimeMillis() - t0;
                    logger.info("{} analyzed in {} ms", input.resourceKey, dt);
                    return Result.ofOk(ImmutableFileResult.builder()
                        .ast(ast)
                        .result(result)
                        .build());
                }, "Analysis for input file " + input.resourceKey + " interrupted");
            })), "Exception when resolving specification");
    }
}
