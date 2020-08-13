package mb.statix.multilang.eclipse;

import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadata;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.SpecConfig;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface LanguageMetadataProvider {
    Map<LanguageId, Supplier<LanguageMetadata>> getLanguageMetadataSuppliers();

    Map<SpecFragmentId, SpecConfig> getSpecConfigs();

    // By default, map all language ids to same context id
    default Map<LanguageId, ContextId> getDefaultLanguageContexts() {
        return getLanguageMetadataSuppliers()
            .keySet()
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                entry -> new ContextId(entry.getId())
            ));
    }
}
