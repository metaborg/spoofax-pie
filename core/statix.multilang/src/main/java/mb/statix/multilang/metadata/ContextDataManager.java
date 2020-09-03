package mb.statix.multilang.metadata;

import java.util.Set;

public interface ContextDataManager {
    Set<LanguageId> getContextLanguages(ContextId contextId);

    ContextId getDefaultContextId(LanguageId languageId);
}
