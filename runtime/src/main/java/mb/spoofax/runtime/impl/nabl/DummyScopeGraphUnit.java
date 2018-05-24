package mb.spoofax.runtime.impl.nabl;

import mb.nabl2.constraints.IConstraint;
import mb.nabl2.solver.Fresh;
import mb.nabl2.solver.ISolution;
import mb.nabl2.spoofax.analysis.CustomSolution;
import mb.nabl2.spoofax.analysis.IScopeGraphUnit;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class DummyScopeGraphUnit implements IScopeGraphUnit {
    private static final long serialVersionUID = 1L;


    @Override public String resource() {
        return "dummy-resource";
    }

    @Override public Set<IConstraint> constraints() {
        return new HashSet<>();
    }

    @Override public Optional<ISolution> solution() {
        return Optional.empty();
    }

    @Override public Optional<CustomSolution> customSolution() {
        return Optional.empty();
    }

    @Override public Fresh fresh() {
        return new Fresh();
    }

    @Override public boolean isPrimary() {
        return false;
    }
}
