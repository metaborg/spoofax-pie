package mb.statix.multilang.metadata;

import mb.common.result.Result;
import mb.statix.multilang.MultiLangAnalysisException;

public interface LanguageMetadataManager {
    Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId);

    default LanguageMetadata getLanguageMetadata(LanguageId languageId) throws MultiLangAnalysisException {
        return getLanguageMetadataResult(languageId).unwrap();
    }
}
