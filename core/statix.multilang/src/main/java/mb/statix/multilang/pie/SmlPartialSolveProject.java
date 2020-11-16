package mb.statix.multilang.pie;

import dagger.Lazy;
import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CUser;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadataManager;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@MultiLangScope
public class SmlPartialSolveProject implements TaskDef<SmlPartialSolveProject.Input, Result<SolverResult, MultiLangAnalysisException>> {
    public static class Input implements Serializable {
        private final Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier;
        private final LanguageId languageId;

        private final @Nullable Level logLevel;

        public Input(
            Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier,
            LanguageId languageId,
            @Nullable Level logLevel
        ) {
            this.globalResultSupplier = globalResultSupplier;
            this.languageId = languageId;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return globalResultSupplier.equals(input.globalResultSupplier) &&
                languageId.equals(input.languageId);
        }

        @Override public int hashCode() {
            return Objects.hash(globalResultSupplier, languageId);
        }

        @Override public String toString() {
            return "Input{" +
                "globalResultSupplier=" + globalResultSupplier +
                ", languageId=" + languageId +
                '}';
        }
    }

    private final SmlBuildSpec buildSpec;
    private final Lazy<LanguageMetadataManager> languageMetadataManager;

    @Inject public SmlPartialSolveProject(SmlBuildSpec buildSpec, @MultiLang Lazy<LanguageMetadataManager> languageMetadataManager) {
        this.buildSpec = buildSpec;
        this.languageMetadataManager = languageMetadataManager;
    }

    @Override
    public String getId() {
        return SmlPartialSolveProject.class.getCanonicalName();
    }

    @Override
    public Result<SolverResult, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        return context.require(input.globalResultSupplier)
            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
            .flatMap(globalResult -> {
                Set<ITerm> scopeArgs = Collections.singleton(globalResult.globalScope());
                return languageMetadataManager.get().getLanguageMetadataResult(input.languageId)
                    .flatMap(lmd -> {
                        IConstraint projectConstraint = new CUser(lmd.projectConstraint(), scopeArgs);

                        IDebugContext debug = SolverUtils.createDebugContext(input.logLevel);
                        return context.require(buildSpec.createSupplier(new SmlBuildSpec.Input(input.languageId)))
                            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                            .flatMap(spec -> {
                                try {
                                    SolverResult res = SolverUtils.partialSolve(spec, State.of(spec).add(globalResult.result().state()),
                                        projectConstraint, debug, new NullProgress(), new NullCancel());
                                    return Result.ofOk(res);
                                } catch(InterruptedException e) {
                                    return Result.ofErr(new MultiLangAnalysisException(e));
                                }
                            });
                    });
            });
    }
}
