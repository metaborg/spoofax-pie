package mb.statix.multilang.eclipse;

import mb.statix.multilang.LanguageMetadata;

public interface LanguageMetadataProvider {
    LanguageMetadata getLanguageMetadata();
    String getAnalysisContextId();
}
