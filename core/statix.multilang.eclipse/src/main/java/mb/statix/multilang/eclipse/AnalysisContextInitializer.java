package mb.statix.multilang.eclipse;

import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.LanguageMetadata;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class AnalysisContextInitializer {
    private static final String ANALYSIS_CONTEXT_ID = "mb.metaborg.statix.multilang.analysiscontext";

    public static void execute(IExtensionRegistry registry) {
        AnalysisContextService analysisContextService = MultiLangPlugin.getComponent().getAnalysisContextService();
        IConfigurationElement[] extensions = registry.getConfigurationElementsFor(ANALYSIS_CONTEXT_ID);

        // Create analysiscontexts for all extensions
        Stream.of(extensions)
            .filter(LanguageMetadataProvider.class::isInstance)
            .map(LanguageMetadataProvider.class::cast)
            .collect(Collectors.groupingBy(LanguageMetadataProvider::getAnalysisContextId))
            .forEach((contextId, providers) -> analysisContextService.createContext(contextId, providers.stream()
                .map(LanguageMetadataProvider::getLanguageMetadata)
                .toArray(LanguageMetadata[]::new)));
    }
}
