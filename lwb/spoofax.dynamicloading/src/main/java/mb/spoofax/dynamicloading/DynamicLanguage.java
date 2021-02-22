package mb.spoofax.dynamicloading;

import mb.log.dagger.LoggerComponent;
import mb.pie.api.serde.JavaSerde;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicLanguage {
    private final URLClassLoader classLoader;
    private final ResourceRegistriesProvider languageResourcesComponent;
    private final ResourceServiceComponent resourceServiceComponent;
    private final LanguageComponent languageComponent;
    private final PieComponent pieComponent;
    private boolean closed = false;

    public DynamicLanguage(
        URL[] classPath,
        String daggerResourcesComponentClassQualifiedId,
        String daggerComponentClassQualifiedId,
        String resourcesComponentClassQualifiedId,
        String resourcesComponentMethodName,
        LoggerComponent loggerComponent,
        ResourceServiceComponent parentResourceServiceComponent,
        PlatformComponent platformComponent,
        RootPieModule pieModule
    ) throws ReflectiveOperationException {
        this.classLoader = new URLClassLoader(classPath, DynamicLanguage.class.getClassLoader());

        {
            final Class<?> daggerClass = classLoader.loadClass(daggerResourcesComponentClassQualifiedId);
            final Method createMethod = daggerClass.getDeclaredMethod("create");
            this.languageResourcesComponent = (ResourceRegistriesProvider)createMethod.invoke(null);
        }

        this.resourceServiceComponent = DaggerResourceServiceComponent.builder()
            .resourceServiceModule(parentResourceServiceComponent.createChildModule(languageResourcesComponent.getResourceRegistries()))
            .loggerComponent(loggerComponent)
            .build();

        {
            final Class<?> daggerClass = classLoader.loadClass(daggerComponentClassQualifiedId);
            final Method builderMethod = daggerClass.getDeclaredMethod("builder");
            final Object builder = builderMethod.invoke(null);
            final Class<?> resourcesComponentClass = classLoader.loadClass(resourcesComponentClassQualifiedId);
            builder.getClass().getDeclaredMethod("loggerComponent", LoggerComponent.class).invoke(builder, loggerComponent);
            builder.getClass().getDeclaredMethod(resourcesComponentMethodName, resourcesComponentClass).invoke(builder, languageResourcesComponent);
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
     * Closes the dynamic language, closing the {@link URLClassLoader classloader} and {@link LanguageComponent language
     * component}, freeing any resources they hold. This dynamic language cannot be used any more after closing it.
     *
     * @throws IOException when {@link URLClassLoader#close() closing the classloader} fails.
     */
    public void close() throws IOException {
        if(closed) return;
        try {
            pieComponent.close();
            classLoader.close();
        } finally {
            closed = true;
        }
    }

    /**
     * Gets the {@link URLClassLoader classloader} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public URLClassLoader getClassLoader() {
        if(closed) throw new IllegalStateException("Cannot get class loader, dynamic language has been closed");
        return classLoader;
    }

    /**
     * Gets the {@link ResourceRegistriesProvider language resources component} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public ResourceRegistriesProvider getLanguageResourcesComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get language resources component, dynamic language has been closed");
        return languageResourcesComponent;
    }

    /**
     * Gets the {@link ResourceServiceComponent resource service component} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public ResourceServiceComponent getResourceServiceComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get resource service component, dynamic language has been closed");
        return resourceServiceComponent;
    }

    /**
     * Gets the {@link LanguageComponent language component} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public LanguageComponent getLanguageComponent() {
        if(closed) throw new IllegalStateException("Cannot get language component, dynamic language has been closed");
        return languageComponent;
    }

    /**
     * Gets the {@link PieComponent PIE component} of this dynamic language.
     *
     * @throws IllegalStateException if the dynamic language has been closed with {@link #close}.
     */
    public PieComponent getPieComponent() {
        if(closed) throw new IllegalStateException("Cannot get PIE component, dynamic language has been closed");
        return pieComponent;
    }

    /**
     * @return true if the dynamic language has been closed with {@link #close}.
     */
    public boolean isClosed() {
        return closed;
    }
}
