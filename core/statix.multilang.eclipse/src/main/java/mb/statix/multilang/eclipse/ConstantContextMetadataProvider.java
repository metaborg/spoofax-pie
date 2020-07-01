package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;

import java.util.Map;
import java.util.Set;

public class ConstantContextMetadataProvider implements ContextMetadataProvider {

    private final Map<ContextId, Set<LanguageId>> contextMetadata;

    protected ConstantContextMetadataProvider(Map<ContextId, Set<LanguageId>> contextMetadata) {
        this.contextMetadata = contextMetadata;
    }

    @Override
    public Map<ContextId, Set<LanguageId>> getContextLanguages() {
        return contextMetadata;
    }
}
