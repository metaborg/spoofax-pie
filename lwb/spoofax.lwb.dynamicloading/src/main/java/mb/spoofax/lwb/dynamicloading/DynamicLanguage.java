package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.common.util.SetView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URLClassLoader;

public class DynamicLanguage {
    protected final ResourcePath rootDirectory;
    protected final CompileLanguageInput compileInput;
    protected final URLClassLoader classLoader;
    protected final ResourceRegistriesProvider resourceRegistriesProvider;
    protected final ResourceServiceComponent resourceServiceComponent;
    protected final LanguageComponent languageComponent;
    protected final PieComponent pieComponent;
    protected boolean closed = false;

    public DynamicLanguage(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        URLClassLoader classLoader,
        ResourceRegistriesProvider resourceRegistriesProvider,
        ResourceServiceComponent resourceServiceComponent,
        LanguageComponent languageComponent,
        PieComponent pieComponent
    ) {
        this.rootDirectory = rootDirectory;
        this.compileInput = compileInput;
        this.classLoader = classLoader;
        this.resourceRegistriesProvider = resourceRegistriesProvider;
        this.resourceServiceComponent = resourceServiceComponent;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
    }


    /**
     * Closes the dynamically loaded language, closing the {@link URLClassLoader classloader} and {@link
     * LanguageComponent language component}, freeing any resources they hold. This dynamically loaded language cannot
     * be used any more after closing it.
     *
     * @throws IOException when {@link URLClassLoader#close() closing the classloader} fails.
     */
    public void close() throws IOException {
        if(closed) return;
        try {
            pieComponent.close();
            languageComponent.close();
            resourceServiceComponent.close();
            classLoader.close();
        } finally {
            closed = true;
        }
    }


    /**
     * Gets the root directory this language was dynamically loaded from.
     */
    public ResourcePath getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Gets the identifier of this dynamically loaded language.
     */
    public String getId() {
        return languageComponent.getLanguageInstance().getId();
    }

    /**
     * Gets the display name of this dynamically loaded language.
     */
    public String getDisplayName() {
        return languageComponent.getLanguageInstance().getDisplayName();
    }

    /**
     * Gets the file extensions of this dynamically loaded language.
     */
    public SetView<String> getFileExtensions() {
        return languageComponent.getLanguageInstance().getFileExtensions();
    }


    /**
     * Gets the {@link CompileLanguageInput compiler input} that was used to compile this dynamically loaded language.
     */
    public CompileLanguageInput getCompileInput() {
        return compileInput;
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

    /**
     * Gets the {@link ResourceRegistriesProvider language resources component} of this dynamically loaded language.
     *
     * @throws IllegalStateException if the dynamically loaded language has been closed with {@link #close}.
     */
    public ResourceRegistriesProvider getResourceRegistriesProvider() {
        if(closed)
            throw new IllegalStateException("Cannot get language resources component, dynamically loaded language has been closed");
        return resourceRegistriesProvider;
    }

    /**
     * Gets the {@link ResourceServiceComponent resource service component} of this dynamically loaded language.
     *
     * @throws IllegalStateException if the dynamically loaded language has been closed with {@link #close}.
     */
    public ResourceServiceComponent getResourceServiceComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get resource service component, dynamically loaded language has been closed");
        return resourceServiceComponent;
    }

    /**
     * Gets the {@link LanguageComponent language component} of this dynamically loaded language.
     *
     * @throws IllegalStateException if the dynamically loaded language has been closed with {@link #close}.
     */
    public LanguageComponent getLanguageComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get language component, dynamically loaded language has been closed");
        return languageComponent;
    }

    /**
     * Gets the {@link PieComponent PIE component} of this dynamically loaded language.
     *
     * @throws IllegalStateException if the dynamically loaded language has been closed with {@link #close}.
     */
    public PieComponent getPieComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get PIE component, dynamically loaded language has been closed");
        return pieComponent;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final DynamicLanguage that = (DynamicLanguage)o;
        return rootDirectory.equals(that.rootDirectory);
    }

    @Override public int hashCode() {
        return rootDirectory.hashCode();
    }

    @Override public String toString() {
        return getDisplayName();
    }
}
