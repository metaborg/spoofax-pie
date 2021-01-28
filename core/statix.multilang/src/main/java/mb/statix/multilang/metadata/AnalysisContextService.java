package mb.statix.multilang.metadata;

import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.metadata.spec.SpecConfig;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service that manages all the statically loaded data used for multilanguage analysis.
 * Note that dynamic configuration (as supplied by {@link mb.statix.multilang.pie.config.SmlReadConfigYaml the
 * yaml config}) can override these values.
 */
@Value.Immutable
public abstract class AnalysisContextService implements LanguageMetadataManager, ContextPieManager, SpecManager {

    @Value.Parameter public abstract Map<LanguageId, ContextId> defaultLanguageContexts();

    @Value.Parameter public abstract Map<LanguageId, Supplier<LanguageMetadata>> languageMetadataSuppliers();

    @Value.Parameter public abstract Map<SpecFragmentId, SpecConfig> specConfigs();

    @Value.Parameter public abstract PieBuilder platformPieBuilder();

    // Map used to cache language metadata instances, so that they will not be recomputed by subsequent accesses.
    private final ConcurrentHashMap<LanguageId, LanguageMetadata> languageMetadataCache = new ConcurrentHashMap<>();

    @Override
    public Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId) {
        if(!languageMetadataSuppliers().containsKey(languageId)) {
            return Result.ofErr(new MultiLangAnalysisException("No language metadata for id " + languageId));
        }

        // Consult cache to return value, else compute from supplier
        return Result.ofOk(languageMetadataCache.computeIfAbsent(languageId, lid -> languageMetadataSuppliers().get(lid).get()));
    }

    @Override public Set<LanguageId> getContextLanguages(ContextId contextId) {
        return defaultLanguageContexts()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(contextId))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    @Override public ContextId getDefaultContextId(LanguageId languageId) {
        return defaultLanguageContexts().getOrDefault(languageId, new ContextId(languageId.getId()));
    }

    @Override public Pie buildPieForContext() throws MultiLangAnalysisException {
        Pie[] languagePies = languageMetadataSuppliers().keySet().stream()
            .map(this::getLanguageMetadataResult)
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Exception fetching language metadata")))
            .unwrap()
            .stream()
            .map(LanguageMetadata::languagePie)
            .toArray(Pie[]::new);

        return platformPieBuilder()
            .build()
            .createChildBuilder(languagePies)
            .build();
    }

    @Value.Check public void checkSpecDependenciesSatisfiable() {
        // Check if each language has spec configuration
        languageMetadataSuppliers().keySet().forEach(lid -> {
            if(!specConfigs().containsKey(new SpecFragmentId(lid.getId()))) {
                throw new IllegalStateException("No spec config for language " + lid);
            }
        });

        // Check if all fragment dependencies are provided
        specConfigs().forEach((specId, conf) -> conf.dependencies().forEach(depId -> {
            if(!specConfigs().containsKey(depId)) {
                throw new IllegalStateException(String.format("Spec %1$s has a dependency on %2$s, but no config for %2$s is found.", specId, depId));
            }
        }));
    }

    public Result<SpecConfig, SpecLoadException> getSpecConfig(SpecFragmentId id) {
        if(specConfigs().containsKey(id)) {
            return Result.ofOk(specConfigs().get(id));
        }
        return Result.ofErr(new SpecLoadException("Unknown fragment id: " + id));
    }

    public static ImmutableAnalysisContextService.Builder builder() {
        return ImmutableAnalysisContextService.builder();
    }
}
