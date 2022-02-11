package mb.spoofax.lwb.dynamicloading.component;

import mb.common.option.Option;
import mb.common.util.SetView;
import mb.pie.dagger.PieComponent;
import mb.pie.runtime.store.SerializingStoreInMemoryBuffer;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.component.Component;
import mb.spoofax.core.component.ComponentImpl;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URLClassLoader;
import java.time.Instant;

public class DynamicComponent implements Component, AutoCloseable {
    private final DynamicComponentInfo info;
    private final Instant created;

    private URLClassLoader classLoader;
    private ComponentImpl component;
    SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer;
    private boolean closed = false;


    public DynamicComponent(
        ResourcePath rootDirectory,
        Coordinate coordinate,
        URLClassLoader classLoader,
        ComponentImpl component,
        SerializingStoreInMemoryBuffer serializingStoreInMemoryBuffer
    ) {
        final String displayName;
        final SetView<String> fileExtensions;
        final @Nullable LanguageComponent languageComponent = component.getLanguageComponent().get();
        if(languageComponent != null) {
            displayName = languageComponent.getLanguageInstance().getDisplayName();
            fileExtensions = languageComponent.getLanguageInstance().getFileExtensions();
        } else {
            displayName = coordinate.toString();
            fileExtensions = SetView.of();
        }
        this.info = new DynamicComponentInfo(rootDirectory, coordinate, displayName, fileExtensions);
        this.created = Instant.now();

        this.classLoader = classLoader;
        this.component = component;
        this.serializingStoreInMemoryBuffer = serializingStoreInMemoryBuffer;
    }

    /**
     * Closes the dynamically loaded language, closing the {@link URLClassLoader classloader} and {@link
     * LanguageComponent language component}, freeing any resources they hold.
     *
     * This dynamically loaded language cannot be used any more after closing it, except the {@link #getInfo} method.
     *
     * @throws IOException when {@link URLClassLoader#close() closing the classloader} fails.
     */
    @SuppressWarnings("ConstantConditions") @Override
    public void close() throws IOException {
        if(closed) return;
        try {
            component.close();
            component = null;
            classLoader.close();
            classLoader = null;
        } finally {
            closed = true;
        }
    }


    @Override public Coordinate getCoordinate() {
        return info.coordinate;
    }

    /**
     * Gets the info of this dynamic component.
     */
    public DynamicComponentInfo getInfo() {
        return info;
    }


    /**
     * Gets the {@link ResourceServiceComponent resource service component} of this dynamically loaded language.
     *
     * @throws IllegalStateException if this has been closed with {@link #close}.
     */
    @Override
    public ResourceServiceComponent getResourceServiceComponent() {
        return component.getResourceServiceComponent();
    }

    /**
     * Gets the {@link LanguageComponent language component} of this dynamically loaded component, or {@code null} if it
     * does not have one.
     *
     * @throws IllegalStateException if the dynamically loaded language has been closed with {@link #close}.
     */
    @Override public Option<LanguageComponent> getLanguageComponent() {
        return component.getLanguageComponent();
    }

    /**
     * Gets the {@link PieComponent PIE component} of this dynamically loaded language.
     *
     * @throws IllegalStateException if this has been closed with {@link #close}.
     */
    @Override
    public PieComponent getPieComponent() {
        return component.getPieComponent();
    }

    @Override public <T> Option<T> getSubcomponent(Class<T> subcomponentType) {
        return component.getSubcomponent(subcomponentType);
    }


    /**
     * @return true if the dynamically loaded language has been closed with {@link #close}.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Gets the {@link URLClassLoader classloader} of this dynamically loaded language.
     *
     * @throws IllegalStateException if the dynamically loaded language has been closed with {@link #close}.
     */
    public URLClassLoader getClassLoader() {
        if(closed)
            throw new IllegalStateException("Cannot get class loader, dynamically loaded language has been closed");
        return classLoader;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final DynamicComponent that = (DynamicComponent)o;
        if(!info.rootDirectory.equals(that.info.rootDirectory)) return false;
        return created.equals(that.created);
    }

    @Override public int hashCode() {
        int result = info.rootDirectory.hashCode();
        result = 31 * result + created.hashCode();
        return result;
    }

    @Override public String toString() {
        return info.toString();
    }
}
