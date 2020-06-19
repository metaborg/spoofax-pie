package mb.statix.multilang.eclipse;

import mb.log.api.Logger;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.statix.multilang.AnalysisContextService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import java.util.stream.Stream;

public class AnalysisContextInitializer {
    private static final String ANALYSIS_CONTEXT_ID = "mb.metaborg.statix.multilang.analysiscontext";
    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory()
        .create(AnalysisContextInitializer.class);

    public static void execute(IExtensionRegistry registry) {
        AnalysisContextService analysisContextService = MultiLangPlugin.getComponent().getAnalysisContextService();
        IConfigurationElement[] extensions = registry.getConfigurationElementsFor(ANALYSIS_CONTEXT_ID);

        // Initialize language metadata providers
        Stream.of(extensions)
            .filter(conf -> conf.getName().equals("languagemetadata"))
            .map(AnalysisContextInitializer::loadClass)
            .filter(LanguageMetadataProvider.class::isInstance)
            .map(LanguageMetadataProvider.class::cast)
            // Register languageMetadata
            .forEach(provider -> provider.getLanguageMetadatas().forEach(entry -> analysisContextService
                .registerLanguageLoader(entry.getKey(), new CachingSupplier<>(entry.getValue()))));

        // Initialize context metadata providers
        Stream.of(extensions)
            .filter(conf -> conf.getName().equals("contextmetadata"))
            .map(AnalysisContextInitializer::loadClass)
            .filter(ContextMetadataProvider.class::isInstance)
            .map(ContextMetadataProvider.class::cast)
            // Register contextId->Language mappings
            .forEach(provider -> provider.getContextConfigurations().forEach(entry -> analysisContextService
                .registerContextLanguageProvider(entry.getKey(), new CachingSupplier<>(entry.getValue()))));

        analysisContextService.initializeService();
    }

    private static Object loadClass(IConfigurationElement conf) {
        try {
            return conf.createExecutableExtension("class");
        } catch(CoreException e) {
            logger.error("Error loading context initializer. Ignoring configuration element.", e);
        }
        return null;
    }
}
