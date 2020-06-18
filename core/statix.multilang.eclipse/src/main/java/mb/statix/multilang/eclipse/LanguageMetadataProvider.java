package mb.statix.multilang.eclipse;

import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;

import java.util.Map;
import java.util.function.Supplier;

public interface LanguageMetadataProvider {
    Iterable<Map.Entry<LanguageId, Supplier<LanguageMetadata>>> getLanguageMetadatas();
}
