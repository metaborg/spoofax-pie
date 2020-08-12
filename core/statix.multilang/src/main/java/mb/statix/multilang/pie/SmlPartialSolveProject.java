package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.nabl2.terms.ITermVar;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CUser;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
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
        private final Supplier<Result<Spec, SpecLoadException>> specSupplier;
        private final Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier;

        private final String projectConstraint;
        private final @Nullable Level logLevel;

        public Input(
            Supplier<Result<GlobalResult, MultiLangAnalysisException>> globalResultSupplier,
            Supplier<Result<Spec, SpecLoadException>> specSupplier,
            String projectConstraint,
            @Nullable Level logLevel
        ) {
            this.globalResultSupplier = globalResultSupplier;
            this.specSupplier = specSupplier;
            this.projectConstraint = projectConstraint;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return globalResultSupplier.equals(input.globalResultSupplier) &&
                specSupplier.equals(input.specSupplier) &&
                projectConstraint.equals(input.projectConstraint);
        }

        @Override public int hashCode() {
            return Objects.hash(globalResultSupplier, specSupplier, projectConstraint);
        }

        @Override public String toString() {
            return "Input{" +
                "globalResultSupplier=" + globalResultSupplier +
                ", specSupplier=" + specSupplier +
                ", projectConstraint='" + projectConstraint + '\'' +
                '}';
        }
    }

    @Inject public SmlPartialSolveProject() {
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
                Set<ITermVar> scopeArgs = Collections.singleton(globalResult.globalScopeVar());
                IConstraint projectConstraint = new CUser(input.projectConstraint, scopeArgs);

                IDebugContext debug = TaskUtils.createDebugContext(input.logLevel);
                return TaskUtils.executeWrapped(() -> context.require(input.specSupplier)
                    .mapErr(MultiLangAnalysisException::wrapIfNeeded)
                    .flatMap(spec -> TaskUtils.executeWrapped(() -> {
                        SolverResult res = SolverUtils.partialSolve(spec, globalResult.result().state(),
                            projectConstraint, debug, new NullProgress(), new NullCancel());
                        return Result.ofOk(res);
                    }, "Project constraint solving interrupted")), "Error loading specification");
            }), "Exception getting global result");
    }
}
