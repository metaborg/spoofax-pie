package mb.statix.multilang;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalysisContextService {

    private static final Map<String, AnalysisContext> contexts = new HashMap<>();

    public static AnalysisContext getAnalysisContext(String contextId) {
        return contexts.get(contextId);
    }

    public static AnalysisContext createContext(String contextId, LanguageMetadata... languageMetadatas) {
        Map<LanguageId, LanguageMetadata> languages = Stream.of(languageMetadatas)
            .collect(Collectors.toMap(LanguageMetadata::languageId, Function.identity()));
        AnalysisContext context = AnalysisContext.builder()
            .contextId(contextId)
            .languages(languages)
            .build();

        contexts.put(contextId, context);

        return context;
    }
}
