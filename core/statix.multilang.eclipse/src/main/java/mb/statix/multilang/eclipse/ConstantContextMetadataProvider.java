package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ConstantContextMetadataProvider implements ContextMetadataProvider {

    private final Map<ContextId, ContextConfig> contextMetadata;

    protected ConstantContextMetadataProvider(Map<ContextId, ContextConfig> contextMetadata) {
        this.contextMetadata = contextMetadata;
    }

    @Override
    public Iterable<Map.Entry<ContextId, ContextConfig>> getContextConfigurations() {
        return contextMetadata.entrySet();
    }
}
