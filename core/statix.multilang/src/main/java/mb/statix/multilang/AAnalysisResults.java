package mb.statix.multilang;

import mb.nabl2.terms.ITerm;
import mb.resource.ResourceKey;
import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;

@Value.Immutable
public abstract class AAnalysisResults implements Serializable {
    public static class FileKey implements Serializable {

        private final LanguageId language;
        private final ResourceKey resource;

        public FileKey(LanguageId language, ResourceKey resource) {
            this.language = language;
            this.resource = resource;
        }

        public LanguageId getLanguage() {
            return language;
        }

        public ResourceKey getResource() {
            return resource;
        }
    }

    @Value.Parameter public abstract ITerm globalScope();

    @Value.Parameter public abstract HashMap<LanguageId, SolverResult> projectResults();

    @Value.Parameter public abstract HashMap<FileKey, SolverResult> fileResults();

    @Value.Parameter public abstract SolverResult finalResult();
}
