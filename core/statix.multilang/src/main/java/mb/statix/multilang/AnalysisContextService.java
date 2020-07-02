package mb.statix.multilang;

import org.immutables.value.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Value.Immutable
public abstract class AnalysisContextService {

    @Value.Parameter public abstract Map<LanguageId, ContextId> defaultLanguageContexts();
    @Value.Parameter public abstract Map<LanguageId, Supplier<LanguageMetadata>> languageMetadataSuppliers();
    @Value.Parameter public abstract Map<ContextId, Set<LanguageId>> contextConfigurations();

    // Map used to cache language metadata instances, so that they will not be recomputed by subsequent accesses.
    private final Map<LanguageId, LanguageMetadata> languageMetadataCache = new HashMap<>();

    public LanguageMetadata getLanguageMetadata(LanguageId languageId) {
        return languageMetadataCache
            // Consult cache to return value
            .computeIfAbsent(languageId, k -> languageMetadataSuppliers()
                // Compute value from supplier
                .computeIfAbsent(languageId, k2 -> {
                    // If no supplier registered, throw exception.
                    throw new MultiLangAnalysisException("No language metadata for id " + languageId);
                })
                .get());
    }

    public Set<LanguageId> getContextLanguages(ContextId contextId) {
        return contextConfigurations().getOrDefault(contextId, Collections.emptySet());
    }

    public ContextId getDefaultContextId(LanguageId languageId) {
        return defaultLanguageContexts().getOrDefault(languageId, new ContextId(languageId.getId()));
    }

    public static ImmutableAnalysisContextService.Builder builder() {
        return ImmutableAnalysisContextService.builder();
    }
}
