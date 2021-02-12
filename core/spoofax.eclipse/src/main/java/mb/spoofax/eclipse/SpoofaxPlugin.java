package mb.spoofax.eclipse;

import mb.common.util.MultiMap;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.core.platform.DaggerResourceServiceComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.spoofax.core.platform.ResourceServiceModule;
import mb.spoofax.eclipse.log.EclipseLoggerFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpoofaxPlugin extends AbstractUIPlugin {
    public static final String id = "spoofax.eclipse";

    private static @Nullable SpoofaxPlugin plugin;
    private static @Nullable EclipseResourceServiceComponent resourceServiceComponent;
    private static @Nullable HashMap<String, ResourceServiceComponent> resourceServiceComponentsPerGroup;
    private static @Nullable EclipsePlatformComponent platformComponent;


    public static SpoofaxPlugin getPlugin() {
        if(plugin == null) {
            throw new RuntimeException(
                "Cannot access SpoofaxPlugin instance; it has not been started yet, or has been stopped");
        }
        return plugin;
    }

    public static EclipseResourceServiceComponent getResourceServiceComponent() {
        if(resourceServiceComponent == null) {
            throw new RuntimeException(
                "Cannot access EclipseResourceServiceComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return resourceServiceComponent;
    }

    public static EclipsePlatformComponent getPlatformComponent() {
        if(platformComponent == null) {
            throw new RuntimeException(
                "Cannot access SpoofaxEclipseComponent; SpoofaxPlugin has not been started yet, or has been stopped");
        }
        return platformComponent;
    }


    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        resourceServiceComponent = DaggerEclipseResourceServiceComponent.create();

        final MultiMap<String, EclipseLanguage> languageGroups = gatherLanguageGroups();

        resourceServiceComponentsPerGroup = new HashMap<>();
        for(Map.Entry<String, ArrayList<EclipseLanguage>> entry : languageGroups.entrySet()) {
            final String group = entry.getKey();
            final ArrayList<EclipseLanguage> eclipseLanguages = entry.getValue();
            final ResourceServiceModule resourceServiceModule = resourceServiceComponent.createChildModule();
            for(EclipseLanguage eclipseLanguage : eclipseLanguages) {
                resourceServiceModule.addRegistriesFrom(eclipseLanguage.createResourcesComponent());
            }
            final ResourceServiceComponent resourceServiceComponent = DaggerResourceServiceComponent.builder()
                .resourceServiceModule(resourceServiceModule)
                .build();
            resourceServiceComponentsPerGroup.put(group, resourceServiceComponent);
        }

        platformComponent = DaggerEclipsePlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(new EclipseLoggerFactory()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .eclipseResourceServiceComponent(resourceServiceComponent)
            .build();
        platformComponent.getPartClosedCallback().register();

        languageGroups.forEachValue((group, language) -> language.createComponent(resourceServiceComponentsPerGroup.get(group), platformComponent));
        languageGroups.forEachValue((group, language) -> language.start(resourceServiceComponentsPerGroup.get(group), platformComponent));
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
        if(platformComponent != null) {
            platformComponent.getColorShare().dispose();
            platformComponent = null;
        }
        resourceServiceComponent = null;
        plugin = null;
    }


    private static MultiMap<String, EclipseLanguage> gatherLanguageGroups() {
        final MultiMap<String, EclipseLanguage> languageGroups = MultiMap.withLinkedHash();
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint("mb.spoofax.eclipse.language");
        for(IConfigurationElement config : point.getConfigurationElements()) {
            if(config.getName().equals("language")) {
                final String group = config.getAttribute("group");
                if(group == null) continue; // TODO: log error
                try {
                    final Object languageObject = config.createExecutableExtension("class");
                    if(languageObject instanceof EclipseLanguage) {
                        final EclipseLanguage language = (EclipseLanguage)languageObject;
                        languageGroups.put(group, language);
                    } else {
                        // TODO: log error
                    }
                } catch(CoreException e) {
                    // TODO: log error
                }
            }
        }
        return languageGroups;
    }
}
