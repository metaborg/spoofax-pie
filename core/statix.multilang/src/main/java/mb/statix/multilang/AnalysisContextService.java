package mb.statix.multilang;

import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.pie.api.Pie;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class AnalysisContextService implements LanguageMetadataManager, ContextPieManager {

    @Value.Parameter public abstract Map<LanguageId, ContextId> defaultLanguageContexts();
    @Value.Parameter public abstract Map<LanguageId, Supplier<LanguageMetadata>> languageMetadataSuppliers();
    @Value.Parameter public abstract Map<ContextId, Set<LanguageId>> contextConfigurations();
    @Value.Parameter public abstract Pie platformPie();

    // Map used to cache language metadata instances, so that they will not be recomputed by subsequent accesses.
    private final ConcurrentHashMap<LanguageId, LanguageMetadata> languageMetadataCache = new ConcurrentHashMap<>();

    @Override public Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId) {
        if(!languageMetadataSuppliers().containsKey(languageId)) {
            return Result.ofErr(new MultiLangAnalysisException("No language metadata for id " + languageId));
        }

        // Consult cache to return value, else compute from supplier
        return Result.ofOk(languageMetadataCache.computeIfAbsent(languageId, lid -> languageMetadataSuppliers().get(lid).get()));
    }

    @Override public Set<LanguageId> getContextLanguages(ContextId contextId) {
        return contextConfigurations().getOrDefault(contextId, Collections.emptySet());
    }

    @Override public ContextId getDefaultContextId(LanguageId languageId) {
        return defaultLanguageContexts().getOrDefault(languageId, new ContextId(languageId.getId()));
    }

    @Override public Pie buildPieForLanguages(Collection<LanguageId> languages) throws MultiLangAnalysisException {
        if(languages.isEmpty()) {
            throw new MultiLangAnalysisException("Cannot build combined Pie when no languages are supplied");
        }

        return languages.stream()
            .map(this::getLanguageMetadataResult)
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Error loading language metadata", false)))
            .map(results -> platformPie().createChildBuilder(results.stream()
                .map(LanguageMetadata::languagePie)
                .toArray(Pie[]::new)).build())
            .unwrap();
    }

    public static ImmutableAnalysisContextService.Builder builder() {
        return ImmutableAnalysisContextService.builder();
    }

    public Pie buildPieForAllTriggeredLanguages() throws MultiLangAnalysisException {
        return buildPieForLanguages(languageMetadataCache.keySet());
    }
}
