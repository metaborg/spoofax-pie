package mb.statix.multilang;

import mb.common.util.MultiHashMap;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.core.platform.Platform;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@MultiLangScope
public class AnalysisContextService {

    private final Map<ContextId, AnalysisContext> contexts = new HashMap<>();
    private final Map<LanguageId, LanguageMetadata> languages = new HashMap<>();
    private final MultiHashMap<ContextId, LanguageId> contextLanguages = new MultiHashMap<>();

    private final Pie basePie;
    private final ResourceService baseResourceService;
    private final Logger logger;

    private boolean serviceInitialized = false;
    private final Object lock = new Object();

    @Inject public AnalysisContextService(
        @Platform Pie basePie,
        @Platform ResourceService baseResourceService,
        LoggerFactory loggerFactory
    ) {
        this.basePie = basePie;
        this.baseResourceService = baseResourceService;
        this.logger = loggerFactory.create(AnalysisContextService.class);
    }

    public void initializeService() {
        serviceInitialized = true;
        synchronized(lock) {
            lock.notifyAll();
        }
    }

    private void ensureContextInitialized(ContextId contextId) {
        waitForServiceInitialized();
        if (contexts.containsKey(contextId)) {
            return;
        }
        synchronized(contexts) {
            if (!contexts.containsKey(contextId)) {
                initializeContext(contextId);
            }
        }

    }

    private void waitForServiceInitialized() {
        if (!serviceInitialized) {
            synchronized(lock) {
                try {
                    lock.wait(3000);
                } catch(InterruptedException e) {
                    throw new MultiLangAnalysisException(e);
                }
            }
        }
    }

    public void registerLanguage(LanguageMetadata languageMetadata) {
        if (serviceInitialized && languages.containsKey(languageMetadata.languageId())) {
            logger.warn("Replacing language metadata after initialization. Already initialized contexts will keep using previously registered metadata");
        }
        languages.put(languageMetadata.languageId(), languageMetadata);
    }

    public void registerContextLanguage(ContextId contextId, Iterable<LanguageId> languageIds) {
        if (serviceInitialized && contexts.containsKey(contextId)) {
            logger.warn("Registering languages for already initialized context.");
            // TODO: Maybe update context?
        }
        contextLanguages.putAll(contextId, languageIds);
    }

    private AnalysisContext initializeContext(ContextId contextId) {
        if (contexts.containsKey(contextId)) {
            throw new MultiLangAnalysisException(String.format("Context with id '%s' is already initialized", contextId));
        }
        if (!contextLanguages.containsKey(contextId)) {
            throw new MultiLangAnalysisException("No configuration for context with id " + contextId);
        }

        Map<LanguageId, LanguageMetadata> languagesForContext = contextLanguages.get(contextId)
            .stream()
            .map(languageId -> languages.computeIfAbsent(languageId, key -> {
                throw new MultiLangAnalysisException("Cannot initialize context " + key +
                    ". No metadata for language " + key);
            }))
            .collect(Collectors.toMap(LanguageMetadata::languageId, Function.identity()));

        AnalysisContext context = ImmutableAnalysisContext.builder()
            .contextId(contextId)
            .languages(languagesForContext)
            .basePie(basePie)
            .baseResourceService(baseResourceService)
            .build();

        contexts.put(contextId, context);

        return context;
    }

    public AnalysisContext getAnalysisContext(ContextId contextId) {
        ensureContextInitialized(contextId);
        return contexts.get(contextId);
    }
}
