package mb.statix.multilang;

import mb.nabl2.terms.ITerm;
import mb.resource.ResourceKey;
import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

@Value.Immutable
public abstract class AnalysisResults implements Serializable {
    @Value.Parameter public abstract ITerm globalScope();

    @Value.Parameter public abstract HashMap<LanguageId, SolverResult> projectResults();

    @Value.Parameter public abstract HashMap<ResourceKey, FileResult> fileResults();

    @Value.Parameter public abstract SolverResult finalResult();

    // Custom toString to prevent memory leaks when logging
    @Override public String toString() {
        return "AnalysisResults{" +
            "globalScope=" + globalScope() +
            ", projectResults=" + projectResults().keySet() +
            ", fileResults=" + fileResults().keySet() +
            ", finalResult=" + finalResult().getClass().getSimpleName() +
            '}';
    }
}
