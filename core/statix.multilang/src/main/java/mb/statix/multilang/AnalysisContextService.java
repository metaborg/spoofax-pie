package mb.statix.multilang;

import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.core.platform.Platform;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MultiLangScope
public class AnalysisContextService {

    private final Map<String, AnalysisContext> contexts = new HashMap<>();
    private final Pie basePie;
    private final ResourceService baseResourceService;

    @Inject public AnalysisContextService(@Platform Pie basePie, @Platform ResourceService baseResourceService) {
        this.basePie = basePie;
        this.baseResourceService = baseResourceService;
    }

    public AnalysisContext getAnalysisContext(String contextId) {
        if (!contexts.containsKey(contextId)) {
            throw new MultiLangAnalysisException(String.format("No context with id '%s'created", contextId));
        }
        return contexts.get(contextId);
    }

    public AnalysisContext createContext(String contextId, LanguageMetadata... languageMetadatas) {
        Map<LanguageId, LanguageMetadata> languages = Stream.of(languageMetadatas)
            .collect(Collectors.toMap(LanguageMetadata::languageId, Function.identity()));

        AnalysisContext context = AnalysisContext.builder()
            .contextId(contextId)
            .languages(languages)
            .basePie(basePie)
            .baseResourceService(baseResourceService)
            .build();

        contexts.put(contextId, context);

        return context;
    }
}
