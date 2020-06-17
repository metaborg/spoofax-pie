package mb.statix.multilang.eclipse;

import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;

import java.util.Map;

public interface LanguageMetadataProvider {
    Iterable<LanguageMetadata> getLanguageMetadatas();
    Iterable<Map.Entry<ContextId, Iterable<LanguageId>>> getContextConfigurations();
}
