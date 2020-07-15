package mb.statix.multilang;

import mb.pie.api.Pie;

import java.util.Collection;

public interface ContextPieManager extends ContextDataManager {
    Pie buildPieForLanguages(Collection<LanguageId> languages) throws MultiLangAnalysisException;
    Pie buildPieForAllTriggeredLanguages() throws MultiLangAnalysisException;
}
