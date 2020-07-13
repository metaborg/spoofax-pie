package mb.statix.multilang;

import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class AnalysisContextService {

    @Value.Parameter public abstract Map<LanguageId, ContextId> defaultLanguageContexts();
    @Value.Parameter public abstract Map<LanguageId, Supplier<LanguageMetadata>> languageMetadataSuppliers();
    @Value.Parameter public abstract Map<ContextId, Set<LanguageId>> contextConfigurations();
    @Value.Parameter public abstract Pie platformPie();

    // Map used to cache language metadata instances, so that they will not be recomputed by subsequent accesses.
    private final Map<LanguageId, LanguageMetadata> languageMetadataCache = new HashMap<>();

    public LanguageMetadata getLanguageMetadata(LanguageId languageId) throws MultiLangAnalysisException {
        if(!languageMetadataSuppliers().containsKey(languageId)) {
            throw new MultiLangAnalysisException("No language metadata for id " + languageId);
        }

        // Consult cache to return value, else compute from supplier
        return languageMetadataCache.computeIfAbsent(languageId, lid -> languageMetadataSuppliers().get(lid).get());
    }

    public Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId) {
        if(!languageMetadataSuppliers().containsKey(languageId)) {
            return Result.ofErr(new MultiLangAnalysisException("No language metadata for id " + languageId));
        }

        // Consult cache to return value, else compute from supplier
        return Result.ofOk(languageMetadataCache.computeIfAbsent(languageId, lid -> languageMetadataSuppliers().get(lid).get()));
    }

    public Set<LanguageId> getContextLanguages(ContextId contextId) {
        return contextConfigurations().getOrDefault(contextId, Collections.emptySet());
    }

    public ContextId getDefaultContextId(LanguageId languageId) {
        return defaultLanguageContexts().getOrDefault(languageId, new ContextId(languageId.getId()));
    }

    public Pie buildPieForLanguages(Collection<LanguageId> languages) throws MultiLangAnalysisException {
        if(languages.isEmpty()) {
            throw new MultiLangAnalysisException("Cannot build combined Pie when no languages are supplied");
        }

        Set<Result<LanguageMetadata, MultiLangAnalysisException>> results = languages.stream()
            .map(this::getLanguageMetadataResult)
            .collect(Collectors.toSet());

        if(results.stream().anyMatch(Result::isErr)) {
            MultiLangAnalysisException exception = new MultiLangAnalysisException("Error loading language metadata");
            results.stream().filter(Result::isErr).map(Result::unwrapErr).forEach(exception::addSuppressed);
            throw exception;
        }

        return platformPie().createChildBuilder(results.stream()
            .map(Result::unwrapUnchecked)
            .map(LanguageMetadata::languagePie)
            .toArray(Pie[]::new)).build();
    }

    public static ImmutableAnalysisContextService.Builder builder() {
        return ImmutableAnalysisContextService.builder();
    }

    public Pie buildPieForAllTriggeredLanguages() throws MultiLangAnalysisException {
        return buildPieForLanguages(languageMetadataCache.keySet());
    }
}
