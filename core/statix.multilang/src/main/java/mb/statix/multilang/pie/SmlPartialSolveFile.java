package mb.statix.multilang.pie;

import dagger.Lazy;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
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
import mb.statix.multilang.pie.spec.SmlBuildSpec;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
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
import java.util.Objects;

@MultiLangScope
public class SmlPartialSolveFile implements TaskDef<SmlPartialSolveFile.Input, Result<FileResult, MultiLangAnalysisException>> {
    public static class Input implements Serializable {
        private final LanguageId languageId;
        private final ResourceKey resourceKey;

        private final @Nullable Level logLevel;

        public Input(
            LanguageId languageId,
            ResourceKey resourceKey,
            @Nullable Level logLevel
        ) {
            this.languageId = languageId;
            this.resourceKey = resourceKey;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return Objects.equals(languageId, input.languageId) &&
                Objects.equals(resourceKey, input.resourceKey);
        }

        @Override public int hashCode() {
            return Objects.hash(languageId, resourceKey);
        }

        @Override public String toString() {
            return "Input{" +
                "languageId=" + languageId +
                ", resourceKey=" + resourceKey +
                '}';
        }
    }

    private final Lazy<LanguageMetadataManager> languageMetadataManager;
    private final SmlInstantiateGlobalScope instantiateGlobalScope;
    private final SmlBuildSpec buildSpec;
    private final Logger logger;

    @Inject public SmlPartialSolveFile(
        @MultiLang Lazy<LanguageMetadataManager> languageMetadataManager,
        SmlInstantiateGlobalScope instantiateGlobalScope,
        SmlBuildSpec buildSpec,
        LoggerFactory loggerFactory
    ) {
        this.languageMetadataManager = languageMetadataManager;
        this.instantiateGlobalScope = instantiateGlobalScope;
        this.buildSpec = buildSpec;
        this.logger = loggerFactory.create(SmlPartialSolveFile.class);
    }

    @Override public String getId() {
        return SmlPartialSolveFile.class.getCanonicalName();
    }

    @Override public Result<FileResult, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        return languageMetadataManager.get().getLanguageMetadataResult(input.languageId)
            .flatMap(languageMetadata -> context.require(languageMetadata.astFunction().createSupplier(input.resourceKey))
                .mapErr(err -> MultiLangAnalysisException.wrapIfNeeded("No ast provided for " + input.resourceKey, err))
                .flatMap((IStrategoTerm ast) -> analyzeAst(context, input, ast)));
    }

    private Result<FileResult, MultiLangAnalysisException> analyzeAst(ExecContext context, Input input, IStrategoTerm ast) {
        return context.require(instantiateGlobalScope.createTask(input.logLevel))
                .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                .flatMap(globalResult -> analyzeForGlobal(context, input, ast, globalResult));
    }

    private Result<FileResult, MultiLangAnalysisException> analyzeForGlobal(ExecContext context, Input input, IStrategoTerm ast, GlobalResult globalResult) {
        return context.require(buildSpec.createSupplier(new SmlBuildSpec.Input(input.languageId)))
            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
            .flatMap(spec -> languageMetadataManager.get().getLanguageMetadataResult(input.languageId).flatMap(languageMetadata -> {
                StrategoTerms st = new StrategoTerms(languageMetadata.termFactory());

                IDebugContext debug = SolverUtils.createDebugContext(input.logLevel);
                Iterable<ITerm> constraintArgs = Arrays.asList(globalResult.globalScope(), st.fromStratego(ast));
                String qualifiedFileConstraintName = String.format("%s:%s", input.languageId.getId(), languageMetadata.fileConstraint());
                IConstraint fileConstraint = new CUser(qualifiedFileConstraintName, constraintArgs, null);

                long t0 = System.currentTimeMillis();
                try {
                    SolverResult result = SolverUtils.partialSolve(spec,
                        State.of(spec)
                            .add(globalResult.result().state())
                            .withResource(input.resourceKey.getIdAsString()),
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
                } catch(InterruptedException e) {
                    return Result.ofErr(MultiLangAnalysisException.wrapIfNeeded(e));
                }
            }));
    }
}
