package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.common.util.SetView;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.serde.JavaSerde;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicLanguage {
    private final ResourcePath rootDirectory;
    private final CompileLanguageInput compileInput;
    private final URLClassLoader classLoader;
    private final ResourceRegistriesProvider languageResourcesComponent;
    private final ResourceServiceComponent resourceServiceComponent;
    private final LanguageComponent languageComponent;
    private final PieComponent pieComponent;
    private boolean closed = false;

    public DynamicLanguage(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        URL[] classPath,
        LoggerComponent loggerComponent,
        ResourceServiceComponent parentResourceServiceComponent,
        PlatformComponent platformComponent,
        RootPieModule pieModule
    ) throws ReflectiveOperationException {
        this.rootDirectory = rootDirectory;
        this.compileInput = compileInput;

        this.classLoader = new URLClassLoader(classPath, DynamicLanguage.class.getClassLoader());

        {
            final Class<?> daggerClass = classLoader.loadClass(compileInput.adapterProjectInput().daggerResourcesComponent().qualifiedId());
            final Method createMethod = daggerClass.getDeclaredMethod("create");
            this.languageResourcesComponent = (ResourceRegistriesProvider)createMethod.invoke(null);
        }

        this.resourceServiceComponent = DaggerResourceServiceComponent.builder()
            .resourceServiceModule(parentResourceServiceComponent.createChildModule(languageResourcesComponent.getResourceRegistries()))
            .loggerComponent(loggerComponent)
            .build();

        {
            final Class<?> daggerClass = classLoader.loadClass(compileInput.adapterProjectInput().daggerComponent().qualifiedId());
            final Method builderMethod = daggerClass.getDeclaredMethod("builder");
            final Object builder = builderMethod.invoke(null);
            final Class<?> resourcesComponentClass = classLoader.loadClass(compileInput.adapterProjectInput().resourcesComponent().qualifiedId());
            builder.getClass().getDeclaredMethod("loggerComponent", LoggerComponent.class).invoke(builder, loggerComponent);
            builder.getClass().getDeclaredMethod(compileInput.adapterProjectInput().resourcesComponent().idAsCamelCase(), resourcesComponentClass).invoke(builder, languageResourcesComponent);
            builder.getClass().getDeclaredMethod("resourceServiceComponent", ResourceServiceComponent.class).invoke(builder, resourceServiceComponent);
            builder.getClass().getDeclaredMethod("platformComponent", PlatformComponent.class).invoke(builder, platformComponent);
            this.languageComponent = (LanguageComponent)builder.getClass().getDeclaredMethod("build").invoke(builder);
        }

        this.pieComponent = DaggerRootPieComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .rootPieModule(pieModule
                .addTaskDefsFrom(languageComponent)
                .withSerdeFactory(__ -> JavaSerde.createWithClassLoaderOverride(classLoader))
            )
            .build();
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
    public ResourceRegistriesProvider getLanguageResourcesComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get language resources component, dynamically loaded language has been closed");
        return languageResourcesComponent;
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
