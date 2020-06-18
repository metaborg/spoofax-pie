package mb.statix.multilang;

import org.metaborg.util.log.Level;

import mb.common.util.MultiHashMap;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.core.platform.Platform;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@MultiLangScope
public class AnalysisContextService {

    private final Map<ContextId, AnalysisContext> contexts = new HashMap<>();
    private final Map<LanguageId, Supplier<LanguageMetadata>> languages = new HashMap<>();
    private final MultiHashMap<ContextId, Supplier<Iterable<ContextConfig>>> contextLanguages = new MultiHashMap<>();

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

    public void registerLanguageLoader(LanguageId languageId, Supplier<LanguageMetadata> languageMetadata) {
        if (serviceInitialized && languages.containsKey(languageId)) {
            logger.warn("Replacing language metadata after initialization. Already initialized contexts will keep using previously registered metadata");
        }
        languages.put(languageId, languageMetadata);
    }

    public void registerContextLanguageProvider(ContextId contextId, Supplier<Iterable<ContextConfig>> configs) {
        if (serviceInitialized && contexts.containsKey(contextId)) {
            logger.warn("Registering languages for already initialized context.");
            // TODO: Maybe update context?
        }
        contextLanguages.put(contextId, configs);
    }

    private AnalysisContext initializeContext(ContextId contextId) {
        if (contexts.containsKey(contextId)) {
            throw new MultiLangAnalysisException(String.format("Context with id '%s' is already initialized", contextId));
        }
        if (!contextLanguages.containsKey(contextId)) {
            throw new MultiLangAnalysisException("No configuration for context with id " + contextId);
        }

        Set<ContextConfig> contextConfigs = contextLanguages.get(contextId)
            .stream()
            .map(Supplier::get)
            .flatMap(languageIds -> StreamSupport.stream(languageIds.spliterator(), false))
            .collect(Collectors.toSet());

        Map<LanguageId, LanguageMetadata> languagesForContext = contextConfigs.stream()
            .map(ContextConfig::getLanguages)
            .flatMap(List::stream)
            .map(LanguageId::new)
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
