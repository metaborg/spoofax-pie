package mb.statix.multilang;

import mb.common.result.Result;

public interface LanguageMetadataManager {
    Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId);

    default LanguageMetadata getLanguageMetadata(LanguageId languageId) throws MultiLangAnalysisException {
        return getLanguageMetadataResult(languageId).unwrap();
    }
}
