package mb.spoofax.lwb.dynamicloading.component;

import mb.common.option.Option;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.api.serde.Serde;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.runtime.store.SerializingStoreInMemoryBuffer;
import mb.resource.ResourceService;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.component.Component;
import mb.spoofax.core.component.ComponentBuilderBase;
import mb.spoofax.core.component.ComponentGroup;
import mb.spoofax.core.component.ComponentImpl;
import mb.spoofax.core.component.Participant;
import mb.spoofax.core.component.ParticipantFactory;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.resource.ResourcesComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DynamicComponentManagerImpl<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends ComponentBuilderBase<L, R, P> implements DynamicComponentManager {
    private final Logger logger;
    private final ListView<Consumer<RootPieModule>> dynamicPieModuleCustomizers;
    private final BiFunction<LoggerFactory, ClassLoader, Serde> serdeFactory;

    private final HashMap<String, DynamicComponent> dynamicComponentPerFileExtension = new HashMap<>();
    private final HashMap<Coordinate, DynamicComponent> dynamicComponentPerCoordinate = new HashMap<>();
    private final HashMap<ResourcePath, DynamicComponent> dynamicComponentPerCompiledSources = new HashMap<>();
    private final Set<DynamicComponentManagerListener> listeners = new LinkedHashSet<>();

    public DynamicComponentManagerImpl(
        L loggerComponent,
        R baseResourceServiceComponent,
        P platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier,
        ListView<Consumer<RootPieModule>> dynamicPieModuleCustomizers,
        BiFunction<LoggerFactory, ClassLoader, Serde> serdeFactory
    ) {
        super(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            pieBuilderSupplier
        );
        this.logger = loggerComponent.getLoggerFactory().create(getClass());
        this.dynamicPieModuleCustomizers = dynamicPieModuleCustomizers;
        this.serdeFactory = serdeFactory;
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
        if(exceptions.size() > 0) {
            final RuntimeException exception = new RuntimeException("Closing one or more dynamically loaded components failed; resources may have been leaked");
            exceptions.forEach(exception::addSuppressed);
            throw exception;
        }
    }


    @Override
    public Option<? extends Component> getComponent(Coordinate coordinate) {
        return getDynamicComponent(coordinate);
    }

    @Override
    public Stream<? extends Component> getComponents() {
        return getDynamicComponents();
    }

    @Override
    public Stream<? extends Component> getComponents(CoordinateRequirement coordinateRequirement) {
        return getDynamicComponents(coordinateRequirement);
    }

    @Override
    public Option<? extends ComponentGroup> getComponentGroup(String group) {
        return Option.ofNone();
    }

    @Override
    public MapView<String, ? extends ComponentGroup> getComponentGroups() {
        return MapView.of();
    }


    @Override
    public Option<ResourcesComponent> getResourcesComponent(Coordinate coordinate) {
        return Option.ofNullable(dynamicComponentPerCoordinate.get(coordinate))
            .flatMap(DynamicComponent::getResourcesComponent);
    }

    @Override
    public Stream<ResourcesComponent> getResourcesComponents(CoordinateRequirement coordinateRequirement) {
        return dynamicComponentPerCompiledSources.values().stream()
            .filter(dc -> coordinateRequirement.matches(dc.getCoordinate()))
            .flatMap(dc -> dc.getResourcesComponent().stream());
    }


    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        return Option.ofNullable(dynamicComponentPerCoordinate.get(coordinate))
            .flatMap(DynamicComponent::getLanguageComponent);
    }

    @Override
    public Stream<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        return dynamicComponentPerCompiledSources.values().stream()
            .filter(dc -> coordinateRequirement.matches(dc.getCoordinate()))
            .flatMap(dc -> dc.getLanguageComponent().stream());
    }

    @Override
    public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType) {
        return Option.ofNullable(dynamicComponentPerCoordinate.get(coordinate))
            .flatMap(dc -> dc.getSubcomponent(subcomponentType));
    }

    @Override
    public <T> Stream<T> getSubcomponents(Class<T> subcomponentType) {
        return dynamicComponentPerCompiledSources.values().stream()
            .flatMap(dc -> dc.getSubcomponent(subcomponentType).stream());
    }

    @Override
    public <T> Stream<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        return dynamicComponentPerCompiledSources.values().stream()
            .filter(dc -> coordinateRequirement.matches(dc.getCoordinate()))
            .flatMap(dc -> dc.getSubcomponent(subcomponentType).stream());
    }


    @Override
    public Option<DynamicComponent> getDynamicComponent(ResourcePath rootDirectory) {
        return Option.ofNullable(dynamicComponentPerCompiledSources.get(rootDirectory));
    }

    @Override
    public Option<DynamicComponent> getDynamicComponent(Coordinate coordinate) {
        return Option.ofNullable(dynamicComponentPerCoordinate.get(coordinate));
    }

    @Override
    public Stream<DynamicComponent> getDynamicComponents() {
        return dynamicComponentPerCompiledSources.values().stream();
    }

    @Override
    public Stream<DynamicComponent> getDynamicComponents(CoordinateRequirement coordinateRequirement) {
        return dynamicComponentPerCompiledSources.values().stream().filter(dc -> coordinateRequirement.matches(dc.getInfo().coordinate));
    }

    @Override
    public Option<DynamicComponent> getDynamicComponent(String fileExtension) {
        return Option.ofNullable(dynamicComponentPerFileExtension.get(fileExtension));
    }


    @Override public DynamicComponent loadOrReloadFromCompiledSources(
        StaticComponentManager staticComponentManager,
        ResourcePath rootDirectory,
        Iterable<ResourcePath> javaClassPaths,
        String participantClassQualifiedId
    ) throws DynamicLoadException {
        // Create classloader
        final URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(classPathToUrl(javaClassPaths, baseResourceServiceComponent.getResourceService()), getClass().getClassLoader());
        } catch(MalformedURLException e) {
            throw DynamicLoadException.classPathToUrlFail(e);
        }

        // Get participant instance.
        final @Nullable ParticipantFactory<L, R, P> participantFactory;
        final Participant<L, R, P> participant;
        try {
            final Class<?> participantClass = classLoader.loadClass(participantClassQualifiedId);
            if(ParticipantFactory.class.isAssignableFrom(participantClass)) {
                // noinspection unchecked (cast is safe due to checks in the next code block)
                participantFactory = (ParticipantFactory<L, R, P>)participantClass.getDeclaredConstructor().newInstance();
                participant = participantFactory.create();
            } else if(Participant.class.isAssignableFrom(participantClass)) {
                participantFactory = null;
                // noinspection unchecked (cast is safe due to checks in the next code block)
                participant = (Participant<L, R, P>)participantClass.getDeclaredConstructor().newInstance();
            } else {
                throw DynamicLoadException.invalidParticipantClassFail(participantClassQualifiedId);
            }
        } catch(ReflectiveOperationException e) {
            throw DynamicLoadException.participantInstantiateFail(e);
        }

        // Check whether the participant is compatible with our classes, otherwise we cannot use `Participant<L, R, P>`
        final Class<? super L> requiredLoggerComponentClass = participant.getRequiredLoggerComponentClass();
        final Class<? extends LoggerComponent> loggerComponentClass = loggerComponent.getClass();
        if(!requiredLoggerComponentClass.isAssignableFrom(loggerComponentClass)) {
            throw DynamicLoadException.incompatibleLoggerComponent(requiredLoggerComponentClass.getName(), loggerComponentClass.getName());
        }
        final Class<? super R> requiredBaseResourceServiceComponentClass = participant.getRequiredBaseResourceServiceComponentClass();
        final Class<? extends ResourceServiceComponent> baseResourceServiceComponentClass = baseResourceServiceComponent.getClass();
        if(!requiredBaseResourceServiceComponentClass.isAssignableFrom(baseResourceServiceComponentClass)) {
            throw DynamicLoadException.incompatibleBaseResourceServiceComponent(requiredBaseResourceServiceComponentClass.getName(), baseResourceServiceComponentClass.getName());
        }
        final Class<? super P> requiredPlatformComponentClass = participant.getRequiredPlatformComponentClass();
        final Class<? extends PlatformComponent> platformComponentClass = platformComponent.getClass();
        if(!requiredPlatformComponentClass.isAssignableFrom(platformComponentClass)) {
            throw DynamicLoadException.incompatiblePlatformComponent(requiredPlatformComponentClass.getName(), platformComponentClass.getName());
        }

        // Check for overlapping coordinates or file extensions. TODO: check with static components as well?
        final Coordinate coordinate = participant.getCoordinate();
        final @Nullable DynamicComponent componentForCoordinates = dynamicComponentPerCoordinate.get(coordinate);
        if(componentForCoordinates != null && !componentForCoordinates.getInfo().rootDirectory.equals(rootDirectory)) {
            throw DynamicLoadException.duplicateCoordinateFail(coordinate, componentForCoordinates.getInfo());
        }
        for(String fileExtension : participant.getLanguageFileExtensions()) {
            final @Nullable DynamicComponent componentForExtension = dynamicComponentPerFileExtension.get(fileExtension);
            if(componentForExtension != null && !componentForExtension.getInfo().rootDirectory.equals(rootDirectory)) {
                throw DynamicLoadException.duplicateFileExtensionFail(fileExtension, componentForExtension.getInfo());
            }
        }

        final @Nullable DynamicComponent previousComponent = dynamicComponentPerCompiledSources.get(rootDirectory);
        final SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer;
        if(previousComponent != null) {
            // Get the PIE store buffer for the existing dynamic component.
            serializingStoreInMemoryBuffer = previousComponent.serializingStoreInMemoryBuffer;
            // Close it to serialize its state to the buffer, and then pass that along to be deserialized later. A
            // serialize-deserialize roundtrip is required because we are closing the classloader of the dynamic
            // component, and thus may not keep any instances of classes of the component around, as this prevents the
            // classes from being garbage collected.
            try {
                previousComponent.close();
            } catch(IOException e) {
                throw DynamicLoadException.closeExistingFail(e);
            }
        } else { // New dynamic component, so just create a new buffer.
            serializingStoreInMemoryBuffer = new SerializingStoreInMemoryBuffer();
        }

        final Stream<Consumer<RootPieModule>> pieModuleCustomizers = Stream.concat(
            staticComponentManager.getPieModuleCustomizers().stream(),
            Stream.concat(
                dynamicPieModuleCustomizers.stream(), // Can override static customizers.
                Stream.of(pieModule -> pieModule // Can override all other customizers, which is necessary for correct serialization/deserialization.
                    .withSerdeFactory(loggerFactory -> serdeFactory.apply(loggerFactory, classLoader))
                    .withStoreFactory((serde, resourceService, loggerFactory) -> SerializingStoreBuilder.ofInMemoryStore(serde)
                        .withInMemoryBuffer(serializingStoreInMemoryBuffer) // Pass in buffer for deserialization.
                        .withLoggingDeserializeFailHandler(loggerFactory)
                        .build()
                    ))
            )
        );
        final BuildOneResult result = super.buildOne( // NOTE: only support dynamically loading a single standalone component right now.
            participantFactory,
            participant,
            staticComponentManager,
            staticComponentManager.getGlobalResourceRegistryProviders().stream(),
            staticComponentManager.getResourceServiceModuleCustomizers().stream(),
            staticComponentManager.getGlobalTaskDefsProviders().stream(),
            pieModuleCustomizers,
            classLoader
        ); // NOTE: global providers from `result` are ignored, as it would require all participants to be reconstructed.
        final ComponentImpl component = result.component;
        final DynamicComponent dynamicComponent = new DynamicComponent(rootDirectory, component.coordinate, classLoader, component, serializingStoreInMemoryBuffer);
        registerComponent(rootDirectory, dynamicComponent);
        component.started(staticComponentManager, this);
        return dynamicComponent;
    }

    @Override public void unloadFromCompiledSources(ResourcePath rootDirectory) {
        final @Nullable DynamicComponent component = dynamicComponentPerCompiledSources.remove(rootDirectory);
        if(component != null) {
            final Set<String> removedFileExtensions = unregisterComponent(component);
            notifyUnload(component, SetView.of(removedFileExtensions));
        }
    }


    @Override public void registerListener(DynamicComponentManagerListener listener) {
        listeners.add(listener);
    }

    @Override public void unregisterListener(DynamicComponentManagerListener listener) {
        listeners.remove(listener);
    }


    private void registerComponent(ResourcePath rootDirectory, DynamicComponent component) {
        final DynamicComponentInfo info = component.getInfo();
        final Coordinate coordinate = info.coordinate;

        final @Nullable DynamicComponent previousComponent = dynamicComponentPerCompiledSources.put(rootDirectory, component);
        final Set<String> previousFileExtensions;
        if(previousComponent != null) {
            previousFileExtensions = unregisterComponent(previousComponent);
        } else {
            previousFileExtensions = Collections.emptySet();
        }
        dynamicComponentPerCoordinate.put(coordinate, component);

        final Set<String> newFileExtensions = info.fileExtensions.asCopy();
        final Set<String> removedFileExtensions = new LinkedHashSet<>(previousFileExtensions);
        removedFileExtensions.removeAll(newFileExtensions);
        final Set<String> addedFileExtensions = new LinkedHashSet<>(newFileExtensions);
        newFileExtensions.removeAll(previousFileExtensions);
        for(String fileExtension : addedFileExtensions) {
            dynamicComponentPerFileExtension.put(fileExtension, component);
        }

        if(previousComponent != null && previousComponent.getInfo().coordinate.equals(coordinate)) {
            notifyReload(previousComponent, component, SetView.of(removedFileExtensions), SetView.of(addedFileExtensions));
        } else {
            notifyLoad(component, SetView.of(addedFileExtensions));
        }
    }

    private Set<String> unregisterComponent(DynamicComponent component) {
        final DynamicComponentInfo info = component.getInfo();
        final Coordinate coordinate = info.coordinate;
        final Set<String> removedFileExtensions = new LinkedHashSet<>();
        dynamicComponentPerCoordinate.remove(coordinate);
        for(String extension : info.fileExtensions) {
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

    private static URL[] classPathToUrl(Iterable<ResourcePath> classPath, ResourceService resourceService) throws MalformedURLException {
        final ArrayList<URL> classPathUrls = new ArrayList<>();
        for(ResourcePath path : classPath) {
            final @Nullable File file = resourceService.toLocalFile(path);
            if(file == null) {
                throw new MalformedURLException("Cannot dynamically load language; resource at path '" + path + "' is not on the local filesystem, and can therefore not be loaded into a URLClassLoader");
            }
            classPathUrls.add(file.toURI().toURL());
        }
        return classPathUrls.toArray(new URL[0]);
    }
}
