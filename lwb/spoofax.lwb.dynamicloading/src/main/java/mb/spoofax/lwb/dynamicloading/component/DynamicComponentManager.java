package mb.spoofax.lwb.dynamicloading.component;

import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.dagger.LoggerComponent;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.component.Component;
import mb.spoofax.core.component.ComponentBuilder;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.Participant;
import mb.spoofax.core.component.StandaloneComponent;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

public class DynamicComponentManager extends ComponentBuilder implements ComponentManager {
    private final Logger logger;
    private final StaticComponentManager staticComponentManager;

    private final HashMap<String, DynamicComponent> dynamicComponentPerFileExtension = new HashMap<>();
    private final HashMap<Coordinate, DynamicComponent> dynamicComponentPerCoordinate = new HashMap<>();
    private final HashMap<ResourcePath, DynamicComponent> dynamicComponentPerCompiledSources = new HashMap<>();
    private final Set<DynamicComponentManagerListener> listeners = new LinkedHashSet<>();

    public DynamicComponentManager(
        StaticComponentManager staticComponentManager
    ) {
        super(
            staticComponentManager.loggerComponent,
            staticComponentManager.baseResourceServiceComponent,
            staticComponentManager.platformComponent,
            staticComponentManager.pieBuilderSupplier
        );
        this.logger = staticComponentManager.loggerComponent.getLoggerFactory().create(getClass());
        this.staticComponentManager = staticComponentManager;
    }

    @Override public void close() {
        listeners.clear();
        final ArrayList<Exception> exceptions = new ArrayList<>();
        dynamicComponentPerCompiledSources.forEach((rootDirectory, language) -> {
            try {
                language.close();
            } catch(IOException e) {
                exceptions.add(e);
            }
        });
        dynamicComponentPerCompiledSources.clear();
        dynamicComponentPerCoordinate.clear();
        dynamicComponentPerFileExtension.clear();
        staticComponentManager.close();
        if(exceptions.size() > 0) {
            final RuntimeException exception = new RuntimeException("Closing one or more dynamically loaded components failed; resources may have been leaked");
            exceptions.forEach(exception::addSuppressed);
            throw exception;
        }
    }


    @Override public LoggerComponent getLoggerComponent() {
        return loggerComponent;
    }

    @Override public PlatformComponent getPlatformComponent() {
        return platformComponent;
    }

    @Override public @Nullable Component getComponent(Coordinate coordinate) {
        return null; // TODO: implement
    }

    @Override public @Nullable LanguageComponent getLanguageComponent(Coordinate coordinate) {
        return null; // TODO: implement
    }


    public @Nullable DynamicComponent getDynamicComponent(ResourcePath rootDirectory) {
        return dynamicComponentPerCompiledSources.get(rootDirectory);
    }

    public @Nullable DynamicComponent getDynamicComponent(Coordinate coordinate) {
        return dynamicComponentPerCoordinate.get(coordinate);
    }

    public @Nullable DynamicComponent getDynamicComponent(String fileExtension) {
        return dynamicComponentPerFileExtension.get(fileExtension);
    }


    public DynamicComponent loadOrReloadFromCompiledSources(
        ResourcePath rootDirectory,
        SortedSet<ResourcePath> javaClassPaths,
        String participantClassQualifiedId
    ) throws IOException, ReflectiveOperationException {
        final URLClassLoader classLoader = new URLClassLoader(classPathToUrl(javaClassPaths, baseResourceServiceComponent.getResourceService()), DynamicComponentManager.class.getClassLoader());
        final Class<?> participantClass = classLoader.loadClass(participantClassQualifiedId);
        final Participant participant = (Participant)participantClass.getDeclaredConstructor().newInstance();
        final BuildOneResult result = super.buildOne( // NOTE: only support dynamically loading a single standalone component right now.
            participant,
            staticComponentManager.globalResourceRegistryProviders,
            staticComponentManager.resourceServiceModuleCustomizers,
            staticComponentManager.globalTaskDefsProviders,
            staticComponentManager.pieModuleCustomizers,
            classLoader
        ); // NOTE: global providers are ignored, as it would require all participants to be reconstructed.
        final StandaloneComponent component = result.component;
        final DynamicComponent dynamicComponent = new DynamicComponent(rootDirectory, component.coordinate, classLoader, component.resourceServiceComponent, component.languageComponent, component.pieComponent);
        registerComponent(rootDirectory, dynamicComponent);
        return dynamicComponent;
    }

    public void unloadFromCompiledSources(ResourcePath rootDirectory) {
        final @Nullable DynamicComponent component = dynamicComponentPerCompiledSources.remove(rootDirectory);
        if(component != null) {
            final Set<String> removedFileExtensions = unregisterComponent(component);
            notifyUnload(component, SetView.of(removedFileExtensions));
        }
    }


    public void registerListener(DynamicComponentManagerListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(DynamicComponentManagerListener listener) {
        listeners.remove(listener);
    }


    private void registerComponent(ResourcePath rootDirectory, DynamicComponent component) {
        final Coordinate coordinate = component.coordinate;
        final @Nullable DynamicComponent componentForCoordinates = dynamicComponentPerCoordinate.get(coordinate);
        if(componentForCoordinates != null && !componentForCoordinates.getRootDirectory().equals(rootDirectory)) {
            logger.error("Cannot register dynamically loaded component '{}' with coordinate '{}', a different component '{}' is already registered with those coordinates. Not loading component", component, coordinate, componentForCoordinates);
            return;
        }

        final @Nullable DynamicComponent previousComponent = dynamicComponentPerCompiledSources.put(rootDirectory, component);
        final Set<String> previousFileExtensions;
        if(previousComponent != null) {
            previousFileExtensions = unregisterComponent(previousComponent);
        } else {
            previousFileExtensions = Collections.emptySet();
        }
        dynamicComponentPerCoordinate.put(coordinate, component);

        final Set<String> newFileExtensions = component.getFileExtensions().asCopy();
        final Set<String> removedFileExtensions = new LinkedHashSet<>(previousFileExtensions);
        removedFileExtensions.removeAll(newFileExtensions);
        final Set<String> addedFileExtensions = new LinkedHashSet<>(newFileExtensions);
        newFileExtensions.removeAll(previousFileExtensions);
        for(String extension : new LinkedHashSet<>(addedFileExtensions) /* Copy because we are removing elements during iteration */) {
            final @Nullable DynamicComponent componentForExtension = dynamicComponentPerFileExtension.get(extension);
            if(componentForExtension != null) {
                logger.warn("Cannot register dynamically loaded component '{}' with a language using file extension '{}', another component '{}' has a language that is already registered with that file extension. Skipping this file extension", component, extension, componentForExtension);
                addedFileExtensions.remove(extension);
                continue;
            }
            dynamicComponentPerFileExtension.put(extension, component);
        }

        if(previousComponent != null && previousComponent.getCoordinate().equals(coordinate)) {
            notifyReload(previousComponent, component, SetView.of(removedFileExtensions), SetView.of(addedFileExtensions));
        } else {
            notifyLoad(component, SetView.of(addedFileExtensions));
        }
    }

    private Set<String> unregisterComponent(DynamicComponent component) {
        final Coordinate coordinate = component.coordinate;
        final Set<String> removedFileExtensions = new LinkedHashSet<>();
        dynamicComponentPerCoordinate.remove(coordinate);
        for(String extension : component.getFileExtensions()) {
            if(dynamicComponentPerFileExtension.remove(extension, component)) {
                removedFileExtensions.add(extension);
            }
        }
        try {
            component.close();
        } catch(IOException e) {
            logger.error("Failed to close dynamically loaded component '{}'; resources may have been leaked", e, component);
        }
        return removedFileExtensions;
    }

    private void notifyLoad(DynamicComponent component, SetView<String> addedFileExtensions) {
        listeners.forEach(l -> l.load(component, addedFileExtensions));
    }

    private void notifyReload(DynamicComponent previousComponent, DynamicComponent component, SetView<String> removedFileExtensions, SetView<String> addedFileExtensions) {
        listeners.forEach(l -> l.reload(previousComponent, component, removedFileExtensions, addedFileExtensions));
    }

    private void notifyUnload(DynamicComponent component, SetView<String> removedFileExtensions) {
        listeners.forEach(l -> l.unload(component, removedFileExtensions));
    }

    private static URL[] classPathToUrl(Iterable<ResourcePath> classPath, ResourceService resourceService) throws IOException {
        final ArrayList<URL> classPathUrls = new ArrayList<>();
        for(ResourcePath path : classPath) {
            final @Nullable File file = resourceService.toLocalFile(path);
            if(file == null) {
                throw new IOException("Cannot dynamically load language; resource at path '" + path + "' is not on the local filesystem, and can therefore not be loaded into a URLClassLoader");
            }
            classPathUrls.add(file.toURI().toURL());
        }
        return classPathUrls.toArray(new URL[0]);
    }
}
