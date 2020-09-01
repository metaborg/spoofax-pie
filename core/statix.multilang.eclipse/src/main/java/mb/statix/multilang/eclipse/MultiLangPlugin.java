package mb.statix.multilang.eclipse;

import mb.log.api.Logger;
import mb.spoofax.eclipse.SpoofaxEclipseComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.statix.multilang.metadata.AnalysisContextService;
import mb.statix.multilang.metadata.ImmutableAnalysisContextService;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiLangPlugin extends Plugin {
    public static final String id = "statix.multilang.eclipse";
    private static final String ANALYSIS_CONTEXT_ID = "mb.statix.multilang.analysiscontext";
    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory().create(MultiLangPlugin.class);

    private static @Nullable MultiLangPlugin plugin;
    private static @Nullable MultiLangEclipseComponent component;

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

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        SpoofaxEclipseComponent platformComponent = SpoofaxPlugin.getComponent();

        component = DaggerMultiLangEclipseComponent
            .builder()
            .multiLangModule(new MultiLangModule(() -> initializeExtensionPoint(Platform.getExtensionRegistry())))
            .platformComponent(platformComponent)
            .build();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
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
