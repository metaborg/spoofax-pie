package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;

import java.util.Map;

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
