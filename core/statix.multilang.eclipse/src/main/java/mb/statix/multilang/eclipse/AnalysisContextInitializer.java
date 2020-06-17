package mb.statix.multilang.eclipse;

import mb.statix.multilang.AnalysisContextService;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import java.util.stream.Stream;

@SuppressWarnings("unused")
public class AnalysisContextInitializer {
    private static final String ANALYSIS_CONTEXT_ID = "mb.metaborg.statix.multilang.analysiscontext";

    public static void execute(IExtensionRegistry registry) {
        AnalysisContextService analysisContextService = MultiLangPlugin.getComponent().getAnalysisContextService();
        IConfigurationElement[] extensions = registry.getConfigurationElementsFor(ANALYSIS_CONTEXT_ID);

        Stream.of(extensions)
            .filter(LanguageMetadataProvider.class::isInstance)
            .map(LanguageMetadataProvider.class::cast)
            .forEach(provider -> {
                // Register contextId->Language mappings
                provider.getContextConfigurations().forEach(entry -> analysisContextService
                    .registerContextLanguage(entry.getKey(), entry.getValue()));
                // Register languageMetadata
                provider.getLanguageMetadatas().forEach(analysisContextService::registerLanguage);
            });

        analysisContextService.initializeService();
    }
}
