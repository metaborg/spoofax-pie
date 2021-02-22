package mb.spoofax.eclipse;

import mb.common.util.MultiMap;
import mb.log.api.Logger;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.log.DaggerEclipseLoggerComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class SpoofaxPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.eclipse";

    private static @Nullable SpoofaxPlugin plugin;
    private static @Nullable EclipseLoggerComponent loggerComponent;
    private static @Nullable Logger logger;
    private static @Nullable EclipseResourceServiceComponent baseResourceServiceComponent;
    private static @Nullable MultiMap<String, EclipseLanguage> languagesPerGroup;
    private static @Nullable HashMap<String, ResourceServiceComponent> resourceServiceComponentsPerGroup;
    private static @Nullable EclipsePlatformComponent platformComponent;
    private static @Nullable MultiMap<String, LanguageComponent> languageComponentsPerGroup;
    private static @Nullable HashMap<String, RootPieComponent> pieComponentsPerGroup;


    public static SpoofaxPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException("Cannot access SpoofaxPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static EclipseLoggerComponent getLoggerComponent() {
        if(loggerComponent == null) {
            throw new RuntimeException("Cannot access EclipseLoggerComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return loggerComponent;
    }

    public static EclipseResourceServiceComponent getResourceServiceComponent() {
        if(baseResourceServiceComponent == null) {
            throw new RuntimeException("Cannot access EclipseResourceServiceComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return baseResourceServiceComponent;
    }

    public static EclipsePlatformComponent getPlatformComponent() {
        if(platformComponent == null) {
            throw new RuntimeException("Cannot access EclipsePlatformComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return platformComponent;
    }


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        final EclipseLoggerComponent loggerComponent = DaggerEclipseLoggerComponent.create();
        SpoofaxPlugin.loggerComponent = loggerComponent;
        final Logger logger = loggerComponent.getLoggerFactory().create(getClass());
        SpoofaxPlugin.logger = logger;

        final EclipseResourceServiceComponent baseResourceServiceComponent = DaggerEclipseResourceServiceComponent.builder()
            .eclipseLoggerComponent(loggerComponent)
            .build();
        SpoofaxPlugin.baseResourceServiceComponent = baseResourceServiceComponent;

        final MultiMap<String, EclipseLanguage> languagesPerGroup = gatherLanguageGroups(logger);
        SpoofaxPlugin.languagesPerGroup = languagesPerGroup;

        // Create resource service components for each language group.
        final HashMap<String, ResourceServiceComponent> resourceServiceComponentsPerGroup = new LinkedHashMap<>();
        SpoofaxPlugin.resourceServiceComponentsPerGroup = resourceServiceComponentsPerGroup;
        languagesPerGroup.forEach((group, languages) -> {
            final ResourceServiceModule resourceServiceModule = baseResourceServiceComponent.createChildModule();
            for(EclipseLanguage language : languages) {
                resourceServiceModule.addRegistriesFrom(language.createResourcesComponent());
            }
            final ResourceServiceComponent resourceServiceComponent = DaggerResourceServiceComponent.builder()
                .loggerComponent(loggerComponent)
                .resourceServiceModule(resourceServiceModule)
                .build();
            resourceServiceComponentsPerGroup.put(group, resourceServiceComponent);
        });

        final EclipsePlatformComponent platformComponent = DaggerEclipsePlatformComponent.builder()
            .eclipseLoggerComponent(loggerComponent)
            .eclipseResourceServiceComponent(baseResourceServiceComponent)
            .build();
        SpoofaxPlugin.platformComponent = platformComponent;
        platformComponent.init();

        // Create language components for each language, using the resource service component of their group.
        final MultiMap<String, LanguageComponent> languageComponentsPerGroup = MultiMap.withLinkedHash();
        SpoofaxPlugin.languageComponentsPerGroup = languageComponentsPerGroup;
        languagesPerGroup.forEach((group, languages) -> {
            final ResourceServiceComponent resourceServiceComponent = resourceServiceComponentsPerGroup.get(group);
            if(resourceServiceComponent == null) {
                throw new RuntimeException("BUG: Cannot get resource service component for language group '" + group + "' which should have been created before");
            }
            for(EclipseLanguage language : languages) {
                final LanguageComponent languageComponent = language.createComponent(loggerComponent, resourceServiceComponent, platformComponent);
                languageComponentsPerGroup.put(group, languageComponent);
            }
        });

        // Create PIE components for each language group, using the task definitions from the languages of the group.
        final HashMap<String, RootPieComponent> pieComponentsPerGroup = new LinkedHashMap<>();
        SpoofaxPlugin.pieComponentsPerGroup = pieComponentsPerGroup;
        languageComponentsPerGroup.forEach((group, languageComponents) -> {
            final ResourceServiceComponent resourceServiceComponent = resourceServiceComponentsPerGroup.get(group);
            if(resourceServiceComponent == null) {
                throw new RuntimeException("BUG: Cannot get resource service component for language group '" + group + "' which should have been created before");
            }
            final RootPieModule pieModule = new RootPieModule(PieBuilderImpl::new);
            for(LanguageComponent languageComponent : languageComponents) {
                pieModule.addTaskDefsFrom(languageComponent);
            }
            final RootPieComponent pieComponent = DaggerRootPieComponent.builder()
                .rootPieModule(pieModule)
                .loggerComponent(loggerComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .build();
            pieComponentsPerGroup.put(group, pieComponent);
        });

        // Start all languages.
        languagesPerGroup.forEachValue((group, language) -> {
            final ResourceServiceComponent resourceServiceComponent = resourceServiceComponentsPerGroup.get(group);
            if(resourceServiceComponent == null) {
                throw new RuntimeException("BUG: Cannot get resource service component for language group '" + group + "' which should have been created before");
            }
            final RootPieComponent pieComponent = pieComponentsPerGroup.get(group);
            if(pieComponent == null) {
                throw new RuntimeException("BUG: Cannot get PIE component for language group '" + group + "' which should have been created before");
            }
            language.start(loggerComponent, resourceServiceComponentsPerGroup.get(group), platformComponent, pieComponent);
        });
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(pieComponentsPerGroup != null) {
            pieComponentsPerGroup.forEach((group, pieComponent) -> {
                try {
                    pieComponent.close();
                } catch(RuntimeException e) {
                    if(logger != null) {
                        logger.error("Closing PIE component '{}' of group '{}' failed unexpectedly", e, pieComponent, group);
                    }
                }
            });
            pieComponentsPerGroup.clear();
            pieComponentsPerGroup = null;
        }
        if(languageComponentsPerGroup != null) {
            // Not closing language components here. Registered languages close it their selves in EclipseLanguage.
            languageComponentsPerGroup.clear();
            languageComponentsPerGroup = null;
        }
        if(platformComponent != null) {
            platformComponent.close();
            platformComponent = null;
        }
        if(resourceServiceComponentsPerGroup != null) {
            resourceServiceComponentsPerGroup.forEach((group, resourceServiceComponent) -> {
                try {
                    resourceServiceComponent.close();
                } catch(Exception e) {
                    if(logger != null) {
                        logger.error("Closing resource service component '{}' of group '{}' failed unexpectedly", e, resourceServiceComponent, group);
                    }
                }
            });
            resourceServiceComponentsPerGroup.clear();
            resourceServiceComponentsPerGroup = null;
        }
        if(languagesPerGroup != null) {
            languagesPerGroup.forEachValue((group, language) -> {
                try {
                    language.close();
                } catch(Exception e) {
                    if(logger != null) {
                        logger.error("Closing language '{}' of group '{}' failed unexpectedly", e, language, group);
                    }
                }
            });
            languagesPerGroup.clear();
            languagesPerGroup = null;
        }
        if(baseResourceServiceComponent != null) {
            try {
                baseResourceServiceComponent.close();
            } catch(Exception e) {
                if(logger != null) {
                    logger.error("Closing base resource service component '{}' failed unexpectedly", e, baseResourceServiceComponent);
                }
            }
            baseResourceServiceComponent = null;
        }
        logger = null;
        loggerComponent = null;
        plugin = null;
    }


    private static MultiMap<String, EclipseLanguage> gatherLanguageGroups(Logger logger) {
        final MultiMap<String, EclipseLanguage> languageGroups = MultiMap.withLinkedHash();
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint("mb.spoofax.eclipse.language");
        final IContributor contributor = point.getContributor();
        for(IConfigurationElement config : point.getConfigurationElements()) {
            if(config.getName().equals("language")) {
                final String group = config.getAttribute("group");
                if(group == null) {
                    logger.error("Found language registration extension point in '{}', but it does not have a 'group' attribute; skipping...", contributor);
                    continue;
                }
                try {
                    final Object languageObject = config.createExecutableExtension("class");
                    if(languageObject == null) {
                        logger.error("Found language registration extension point in '{}', but it does not have a 'class' property; skipping...", contributor);
                    }
                    if(languageObject instanceof EclipseLanguage) {
                        final EclipseLanguage language = (EclipseLanguage)languageObject;
                        languageGroups.put(group, language);
                    } else {
                        logger.error("Found language registration extension point in '{}', but the object '{}' instantiated from its 'class' property does not implement 'EclipseLanguage'; skipping...", contributor, languageObject);
                    }
                } catch(CoreException | InvalidRegistryObjectException e) {
                    logger.error("Found language registration extension point in '{}', but instantiating an object of its 'class' property failed unexpectedly; skipping...", e, contributor);
                }
            }
        }
        return languageGroups;
    }
}
