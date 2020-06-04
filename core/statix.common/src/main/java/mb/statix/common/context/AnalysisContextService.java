package mb.statix.common.context;

import mb.nabl2.terms.unification.OccursException;

import java.util.HashMap;
import java.util.Map;

public class AnalysisContextService {

    private static final Map<String, AnalysisContext> contexts = new HashMap<>();

    public static AnalysisContext getAnalysisContext(String contextId) throws OccursException {
        if (contexts.containsKey(contextId)) {
            return contexts.get(contextId);
        }
        synchronized(contexts) {
            AnalysisContext context = new AnalysisContext();
            contexts.put(contextId, context);
            return context;
        }
    }
}
