package mb.spoofax.runtime.impl.nabl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.solver.Fresh;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.IScopeGraphUnit;

public class DummyScopeGraphUnit implements IScopeGraphUnit {
    private static final long serialVersionUID = 1L;


    @Override public String resource() {
        return "dummy-resource";
    }

    @Override public Set<IConstraint> constraints() {
        return new HashSet<>();
    }

    @Override public Optional<Solution> solution() {
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
