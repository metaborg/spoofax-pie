package mb.constraint.common;

import mb.constraint.common.ConstraintAnalyzer.Result;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ConstraintAnalyzerContext implements Serializable {
    // TODO: serializing hashmap may cause problems because it is not equal after a round-trip?
    private final HashMap<ResourceKey, Result> results = new HashMap<>();


    public @Nullable Result getResult(ResourceKey resource) {
        return results.get(resource);
    }

    public Set<Entry<ResourceKey, Result>> getResultEntries() {
        return results.entrySet();
    }

    public Set<ResourceKey> getResultResources() {
        return results.keySet();
    }


    void updateResult(ResourceKey resource, IStrategoTerm ast, IStrategoTerm analysis) {
        results.put(resource, new Result(resource, ast, analysis));
    }

    void updateResult(ResourceKey resource, IStrategoTerm analysis) {
        final @Nullable Result result = results.get(resource);
        if(result == null) {
            results.put(resource, new Result(resource, null, analysis));
        } else {
            results.put(resource, new Result(resource, result.ast, analysis));
        }
    }

    void removeResult(ResourceKey resource) {
        results.remove(resource);
    }
}
