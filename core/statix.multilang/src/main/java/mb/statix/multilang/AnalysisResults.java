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

    @Value.Parameter public abstract HashMap<FileKey, FileResult> fileResults();

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

    // TODO: Remove and replace usages with ResourceKey
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

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            FileKey fileKey = (FileKey)o;
            return Objects.equals(language, fileKey.language) &&
                Objects.equals(resource, fileKey.resource);
        }

        @Override
        public int hashCode() {
            return Objects.hash(language, resource);
        }

        @Override public String toString() {
            return "FileKey{" +
                "language=" + language +
                ", resource=" + resource +
                '}';
        }
    }
}
