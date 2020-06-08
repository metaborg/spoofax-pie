package mb.statix.multilang;

import mb.resource.ResourceKey;
import mb.statix.scopegraph.terms.Scope;
import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;

@Value.Immutable
public abstract class AAnalysisResults implements Serializable {
    public static class FileKey implements Serializable {
        private LanguageId language;
        private ResourceKey resource;

        public FileKey(LanguageId language, ResourceKey resource) {
            this.language = language;
            this.resource = resource;
        }
    }

    @Value.Parameter public abstract Scope globalScope();

    @Value.Parameter public abstract HashMap<LanguageId, SolverResult> projectResults();

    @Value.Parameter public abstract HashMap<FileKey, SolverResult> fileResults();

    @Value.Parameter public abstract SolverResult finalResult();
}
