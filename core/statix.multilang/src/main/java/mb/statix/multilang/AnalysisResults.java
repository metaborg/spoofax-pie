package mb.statix.multilang;

import mb.nabl2.terms.ITerm;
import mb.resource.ResourceKey;
import mb.statix.solver.persistent.SolverResult;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;

@Value.Immutable
public interface AnalysisResults extends Serializable {
    class FileKey implements Serializable {

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

    @Value.Parameter ITerm globalScope();

    @Value.Parameter HashMap<LanguageId, SolverResult> projectResults();

    @Value.Parameter HashMap<FileKey, FileResult> fileResults();

    @Value.Parameter SolverResult finalResult();
}
