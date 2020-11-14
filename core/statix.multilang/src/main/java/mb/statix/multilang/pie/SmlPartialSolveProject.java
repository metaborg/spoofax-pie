package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CUser;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
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

        private final String projectConstraint;
        private final @Nullable Level logLevel;

        public Input(
            Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier,
            LanguageId languageId,
            String projectConstraint,
            @Nullable Level logLevel
        ) {
            this.globalResultSupplier = globalResultSupplier;
            this.languageId = languageId;
            this.projectConstraint = projectConstraint;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return globalResultSupplier.equals(input.globalResultSupplier) &&
                languageId.equals(input.languageId) &&
                projectConstraint.equals(input.projectConstraint);
        }

        @Override public int hashCode() {
            return Objects.hash(globalResultSupplier, languageId, projectConstraint);
        }

        @Override public String toString() {
            return "Input{" +
                "globalResultSupplier=" + globalResultSupplier +
                ", languageId=" + languageId +
                ", projectConstraint='" + projectConstraint + '\'' +
                '}';
        }
    }

    private final SmlBuildSpec buildSpec;

    @Inject public SmlPartialSolveProject(SmlBuildSpec buildSpec) {
        this.buildSpec = buildSpec;
    }

    @Override
    public String getId() {
        return SmlPartialSolveProject.class.getCanonicalName();
    }

    @Override
    public Result<SolverResult, MultiLangAnalysisException> exec(ExecContext context, Input input) {
        return TaskUtils.executeWrapped(() -> context.require(input.globalResultSupplier)
            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
            .flatMap(globalResult -> {
                Set<ITerm> scopeArgs = Collections.singleton(globalResult.globalScope());
                IConstraint projectConstraint = new CUser(input.projectConstraint, scopeArgs);

                IDebugContext debug = TaskUtils.createDebugContext(input.logLevel);
                return TaskUtils.executeWrapped(() -> context.require(buildSpec.createSupplier(new SmlBuildSpec.Input(input.languageId)))
                    .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                    .flatMap(spec -> TaskUtils.executeWrapped(() -> {
                        SolverResult res = SolverUtils.partialSolve(spec, State.of(spec).add(globalResult.result().state()),
                            projectConstraint, debug, new NullProgress(), new NullCancel());
                        return Result.ofOk(res);
                    }, "Project constraint solving interrupted")), "Error loading specification");
            }), "Exception getting global result");
    }
}
