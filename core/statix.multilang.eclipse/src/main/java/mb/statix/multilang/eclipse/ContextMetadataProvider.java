package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;

import java.util.Map;
import java.util.Set;

public interface ContextMetadataProvider {
    Map<ContextId, Set<LanguageId>> getContextLanguages();
}
