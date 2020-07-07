package mb.statix.multilang.pie;

import mb.nabl2.terms.ITermVar;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.statix.constraints.CUser;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.utils.SolverUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.Level;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@MultiLangScope
public class SmlPartialSolveProject implements TaskDef<SmlPartialSolveProject.Input, SolverResult> {

    public static class Input implements Serializable {
        private final Supplier<Spec> specSupplier;
        private final Supplier<GlobalResult> globalResultSupplier;

        private final String projectConstraint;
        private final @Nullable Level logLevel;

        public Input(Supplier<GlobalResult> globalResultSupplier, Supplier<Spec> specSupplier, String projectConstraint,
                     @Nullable Level logLevel) {
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

        @Override
        public int hashCode() {
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
        return SmlPartialSolveProject.class.getSimpleName();
    }

    @Override
    public SolverResult exec(ExecContext context, Input input) throws Exception {
        GlobalResult globalResult = context.require(input.globalResultSupplier);
        Iterable<ITermVar> scopeArgs = Iterables2.singleton(globalResult.getGlobalScopeVar());
        IConstraint projectConstraint = new CUser(input.projectConstraint, scopeArgs);

        IDebugContext debug = TaskUtils.createDebugContext(SmlPartialSolveFile.class, input.logLevel);
        Spec spec = context.require(input.specSupplier);

        return SolverUtils.partialSolve(spec, globalResult.getResult().state(), projectConstraint, debug,
            new NullProgress(), new NullCancel());
    }
}
