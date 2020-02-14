package mb.constraint.common;

import mb.constraint.common.ConstraintAnalyzer.Result;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ConstraintAnalyzerContext {
    private final HashMap<ResourceKey, Result> results = new HashMap<>();
//    private final HashMap<String, ResourceKey> resources = new HashMap<>();


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


//    public @Nullable ResourceKey getResource(String str) {
//        return resources.get(str);
//    }
//
//    void registerResource(ResourceKey resource) {
//        resources.put(resource.toString(), resource);
//    }
//
//    void removeResource(ResourceKey resource) {
//        resources.remove(resource.toString());
//    }
}
