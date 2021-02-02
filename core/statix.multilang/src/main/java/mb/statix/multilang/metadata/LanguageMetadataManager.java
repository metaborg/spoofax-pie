package mb.statix.multilang.metadata;

import mb.common.result.Result;
import mb.statix.multilang.MultiLangAnalysisException;

public interface LanguageMetadataManager {
    Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId);
}
