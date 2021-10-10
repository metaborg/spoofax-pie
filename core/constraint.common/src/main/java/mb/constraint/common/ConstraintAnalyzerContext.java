package mb.constraint.common;

import mb.constraint.common.ConstraintAnalyzer.ProjectResult;
import mb.constraint.common.ConstraintAnalyzer.Result;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ConstraintAnalyzerContext implements Serializable {
    private final boolean multiFile;
    private final @Nullable ResourceKey root;

    private final LinkedHashMap<ResourceKey, Result> results = new LinkedHashMap<>();
    private final LinkedHashMap<ResourceKey, ProjectResult> projectResults = new LinkedHashMap<>();


    public ConstraintAnalyzerContext(boolean multiFile, @Nullable ResourceKey root) {
        this.multiFile = multiFile;
        this.root = root;
    }


    public @Nullable Result getResult(ResourceKey resource) {
        return results.get(resource);
    }

    public @Nullable IStrategoTerm getAnalysisTerm(ResourceKey resource) {
        if(multiFile && root != null) {
            final @Nullable ProjectResult projectResult = projectResults.get(root);
            if(projectResult != null) {
                return projectResult.analysis;
            }
        }
        return results.get(resource).analysis;
    }

    public Set<Entry<ResourceKey, Result>> getResultEntries() {
        return results.entrySet();
    }

    public Set<ResourceKey> getResultResources() {
        return results.keySet();
    }

    void updateResult(ResourceKey resource, IStrategoTerm parsedAst, IStrategoTerm analyzedAst, IStrategoTerm analysis) {
        results.put(resource, new Result(resource, parsedAst, analyzedAst, analysis));
    }

    void updateResult(ResourceKey resource, IStrategoTerm analysis) {
        final @Nullable Result result = results.get(resource);
        if(result == null) {
            throw new RuntimeException("BUG: attempting to update analysis result for '" + resource + "' to '" + analysis + "', but no existing result was found for it");
        } else {
            results.put(resource, new Result(resource, result.parsedAst, result.analyzedAst, analysis));
        }
    }

    void removeResult(ResourceKey resource) {
        results.remove(resource);
    }


    public @Nullable ProjectResult getProjectResult(ResourceKey resource) {
        return projectResults.get(resource);
    }

    public Set<Entry<ResourceKey, ProjectResult>> getProjectResultEntries() {
        return projectResults.entrySet();
    }

    public Set<ResourceKey> getProjectResultResources() {
        return projectResults.keySet();
    }

    void updateProjectResult(ResourceKey resource, IStrategoTerm analysis) {
        projectResults.put(resource, new ProjectResult(resource, analysis));
    }

    void removeProjectResult(ResourceKey resource) {
        projectResults.remove(resource);
    }


    public void clear() {
        results.clear();
        projectResults.clear();
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ConstraintAnalyzerContext that = (ConstraintAnalyzerContext)o;
        if(multiFile != that.multiFile) return false;
        if(root != null ? !root.equals(that.root) : that.root != null) return false;
        if(!results.equals(that.results)) return false;
        return projectResults.equals(that.projectResults);
    }

    @Override public int hashCode() {
        int result = (multiFile ? 1 : 0);
        result = 31 * result + (root != null ? root.hashCode() : 0);
        result = 31 * result + results.hashCode();
        result = 31 * result + projectResults.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ConstraintAnalyzerContext{" +
            "multiFile=" + multiFile +
            ", root=" + root +
            ", results=" + results +
            ", projectResults=" + projectResults +
            '}';
    }
}
