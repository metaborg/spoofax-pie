package mb.statix.multilang.eclipse;

import mb.log.api.Logger;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.job.LockRule;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.ImmutableAnalysisContextService;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.MultiLangModule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiLangPlugin extends Plugin {
    public static final String id = "statix.multilang.eclipse";
    private static final String ANALYSIS_CONTEXT_ID = "mb.statix.multilang.analysiscontext";
    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory().create(MultiLangPlugin.class);

    private static @Nullable MultiLangPlugin plugin;
    private static @Nullable MultiLangEclipseComponent component;

    private static @Nullable ConfigResourceChangeListener configResourceChangeListener;

    public static MultiLangPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access MultiLangPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static MultiLangEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access MultiLangComponent; MultiLangPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    public static ConfigResourceChangeListener getConfigResourceChangeListener() {
        return configResourceChangeListener;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        SpoofaxEclipseComponent platformComponent = SpoofaxPlugin.getComponent();

        component = DaggerMultiLangEclipseComponent
            .builder()
            .multiLangModule(new MultiLangModule(() -> initializeExtensionPoint(Platform.getExtensionRegistry())))
            .platformComponent(platformComponent)
            .build();

        // Add listener to multilang.yaml files, which triggers analysis updates
        configResourceChangeListener = new ConfigResourceChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(configResourceChangeListener);
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(configResourceChangeListener);
        configResourceChangeListener.clearDelegates();
    }

    private static AnalysisContextService initializeExtensionPoint(IExtensionRegistry registry) {
        IConfigurationElement[] extensions = registry.getConfigurationElementsFor(ANALYSIS_CONTEXT_ID);
        ImmutableAnalysisContextService.Builder analysisContextServiceBuilder = AnalysisContextService.builder();

        // Initialize language metadata providers
        List<LanguageMetadataProvider> languageMetadataProviders = Stream.of(extensions)
            .filter(conf -> conf.getName().equals("languagemetadata"))
            .map(MultiLangPlugin::loadClass)
            .filter(LanguageMetadataProvider.class::isInstance)
            .map(LanguageMetadataProvider.class::cast)
            .collect(Collectors.toList());

        // Register suppliers
        languageMetadataProviders.stream()
            .map(LanguageMetadataProvider::getLanguageMetadataSuppliers)
            .forEach(analysisContextServiceBuilder::putAllLanguageMetadataSuppliers);

        // Register default context ids
        languageMetadataProviders.stream()
            .map(LanguageMetadataProvider::getDefaultLanguageContexts)
            .forEach(analysisContextServiceBuilder::putAllDefaultLanguageContexts);

        // Initialize languages for context
        Stream.of(extensions)
            // Collect all valid context metadata suppliers
            .filter(conf -> conf.getName().equals("contextmetadata"))
            .map(MultiLangPlugin::loadClass)
            .filter(ContextMetadataProvider.class::isInstance)
            .map(ContextMetadataProvider.class::cast)
            .map(ContextMetadataProvider::getContextLanguages)
            // Configuration for a context can come from different sources, so there may be duplicate keys in the maps
            // However, Immutables builders dont accept those. Therefore we merge all lists associated with the same key
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue,
                // When encountering duplicate contextIds, merge the 2 language id sets
                // By using sets, it deduplicates for free
                (left, right) -> {
                    HashSet<LanguageId> result = new HashSet<>(left);
                    result.addAll(right);
                    return result;
                }))
            .forEach(analysisContextServiceBuilder::putContextConfigurations);

        return analysisContextServiceBuilder
            .platformPie(SpoofaxPlugin.getComponent().getPie())
            .build();
    }

    private static @Nullable Object loadClass(IConfigurationElement conf) {
        try {
            return conf.createExecutableExtension("class");
        } catch(CoreException e) {
            logger.warn("Error loading context initializer. Ignoring configuration element.", e);
        }
        return null;
    }

}
