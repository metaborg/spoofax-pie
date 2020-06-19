package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;

import java.util.Map;

public interface ContextMetadataProvider {
    Iterable<Map.Entry<ContextId, ContextConfig>> getContextConfigurations();
}
