package mb.statix.multilang.metadata;

import mb.pie.api.Pie;
import mb.statix.multilang.MultiLangAnalysisException;

import java.util.Collection;

public interface ContextPieManager extends ContextDataManager {
    Pie buildPieForContext() throws MultiLangAnalysisException;
}
