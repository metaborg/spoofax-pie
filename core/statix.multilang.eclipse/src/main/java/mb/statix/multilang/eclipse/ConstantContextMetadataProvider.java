package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ConstantContextMetadataProvider implements ContextMetadataProvider {

    private final Map<ContextId, Iterable<ContextConfig>> contextMetadata;

    protected ConstantContextMetadataProvider(Map<ContextId, Iterable<ContextConfig>> contextMetadata) {
        this.contextMetadata = contextMetadata;
    }

    @Override
    public Iterable<Map.Entry<ContextId, Supplier<Iterable<ContextConfig>>>> getContextConfigurations() {
        Stream<Map.Entry<ContextId, Supplier<Iterable<ContextConfig>>>> i = contextMetadata
            .entrySet()
            .stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry::getValue));

        return i::iterator;
    }
}
