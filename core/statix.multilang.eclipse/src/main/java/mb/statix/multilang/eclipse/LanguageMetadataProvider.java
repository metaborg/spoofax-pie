package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface LanguageMetadataProvider {
    Map<LanguageId, Supplier<LanguageMetadata>> getLanguageMetadataSuppliers();

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
