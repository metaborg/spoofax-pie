package mb.statix.multilang;

import mb.common.util.MultiHashMap;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.core.platform.Platform;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MultiLangScope
public class AnalysisContextService {

    private final Map<ContextId, AnalysisContext> contexts = new HashMap<>();
    private final Map<LanguageId, Supplier<LanguageMetadata>> languages = new HashMap<>();
    private final MultiHashMap<ContextId, ContextConfig> contextConfigurations = new MultiHashMap<>();

    private final Pie basePie;
    private final ResourceService baseResourceService;
    private final LoggerFactory loggerFactory;
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
        this.loggerFactory = loggerFactory;
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
        if(contexts.containsKey(contextId)) {
            return;
        }
        synchronized(contexts) {
            if(!contexts.containsKey(contextId)) {
                initializeContext(contextId);
            }
        }
    }

    private void waitForServiceInitialized() {
        if(!serviceInitialized) {
            synchronized(lock) {
                try {
                    lock.wait(3000);
                } catch(InterruptedException e) {
                    throw new MultiLangAnalysisException(e);
                }
            }
        }
    }

    public void registerLanguageLoader(LanguageId languageId, Supplier<LanguageMetadata> languageMetadata) {
        if(serviceInitialized && languages.containsKey(languageId)) {
            logger.warn("Replacing language metadata after initialization. Already initialized contexts will keep using previously registered metadata");
        }
        languages.put(languageId, languageMetadata);
    }

    public void registerContextConfig(ContextId contextId, ContextConfig configs) {
        if(serviceInitialized && contexts.containsKey(contextId)) {
            logger.warn("Registering languages for already initialized context.");
            // TODO: Maybe update context?
        }
        contextConfigurations.put(contextId, configs);
    }

    private AnalysisContext initializeContext(ContextId contextId) {
        if(contexts.containsKey(contextId)) {
            throw new MultiLangAnalysisException(String.format("Context with id '%s' is already initialized", contextId));
        }
        if(!contextConfigurations.containsKey(contextId)) {
            throw new MultiLangAnalysisException("No configuration for context with id " + contextId);
        }

        ArrayList<ContextConfig> contextConfigs = contextConfigurations.get(contextId);

        Map<LanguageId, LanguageMetadata> languagesForContext = contextConfigs.stream()
            .map(ContextConfig::getLanguages)
            .flatMap(List::stream)
            .map(languageId -> languages.computeIfAbsent(languageId, key -> {
                throw new MultiLangAnalysisException("Cannot initialize context " + key +
                    ". No metadata for language " + key);
            }))
            .map(Supplier::get)
            .collect(Collectors.toMap(LanguageMetadata::languageId, Function.identity()));

        Level logLevel = contextConfigs.stream()
            .map(ContextConfig::parseLevel)
            .filter(Objects::nonNull)
            .min(Comparator.comparing(Enum::ordinal))
            .orElse(null);

        AnalysisContext context = ImmutableAnalysisContext.builder()
            .contextId(contextId)
            .languages(languagesForContext)
            .basePie(basePie)
            .baseResourceService(baseResourceService)
            .logger(loggerFactory.create(String.format("MLA [%s]", contextId)))
            .stxLogLevel(logLevel)
            .build();

        contexts.put(contextId, context);

        return context;
    }

    public AnalysisContext getAnalysisContext(ContextId contextId) {
        ensureContextInitialized(contextId);
        return contexts.get(contextId);
    }
}
