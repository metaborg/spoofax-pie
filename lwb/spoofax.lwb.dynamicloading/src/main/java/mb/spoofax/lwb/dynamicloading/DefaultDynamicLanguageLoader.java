package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
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

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@DynamicLoadingScope
public class DefaultDynamicLanguageLoader implements DynamicLanguageLoader {
    private final LoggerComponent loggerComponent;
    private final ResourceServiceComponent parentResourceServiceComponent;
    private final PlatformComponent platformComponent;
    private final Provider<RootPieModule> rootPieModuleProvider;

    @Inject public DefaultDynamicLanguageLoader(
        LoggerComponent loggerComponent,
        ResourceServiceComponent parentResourceServiceComponent,
        PlatformComponent platformComponent,
        Provider<RootPieModule> rootPieModuleProvider
    ) {
        this.loggerComponent = loggerComponent;
        this.parentResourceServiceComponent = parentResourceServiceComponent;
        this.platformComponent = platformComponent;
        this.rootPieModuleProvider = rootPieModuleProvider;
    }

    @Override public DynamicLanguage load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        List<ResourcePath> classPath
    ) throws ReflectiveOperationException, IOException {
        final ArrayList<URL> classPathUrls = new ArrayList<>();
        for(ResourcePath path : classPath) {
            final @Nullable File file = parentResourceServiceComponent.getResourceService().toLocalFile(path);
            if(file == null) {
                throw new IOException("Cannot dynamically load language; resource at path '" + path + "' is not on the local filesystem, and can therefore not be loaded into a URLClassLoader");
            }
            classPathUrls.add(file.toURI().toURL());
        }
        final URLClassLoader classLoader = new URLClassLoader(classPathUrls.toArray(new URL[0]), DynamicLanguage.class.getClassLoader());

        final ResourceRegistriesProvider resourceRegistriesProvider;
        {
            final Class<?> daggerClass = classLoader.loadClass(compileInput.adapterProjectInput().daggerResourcesComponent().qualifiedId());
            final Method createMethod = daggerClass.getDeclaredMethod("create");
            resourceRegistriesProvider = (ResourceRegistriesProvider)createMethod.invoke(null);
        }

        final ResourceServiceComponent resourceServiceComponent = DaggerResourceServiceComponent.builder()
            .resourceServiceModule(parentResourceServiceComponent.createChildModule(resourceRegistriesProvider.getResourceRegistries()))
            .loggerComponent(loggerComponent)
            .build();

        final LanguageComponent languageComponent;
        {
            final Class<?> daggerClass = classLoader.loadClass(compileInput.adapterProjectInput().daggerComponent().qualifiedId());
            final Method builderMethod = daggerClass.getDeclaredMethod("builder");
            final Object builder = builderMethod.invoke(null);
            final Class<?> resourcesComponentClass = classLoader.loadClass(compileInput.adapterProjectInput().resourcesComponent().qualifiedId());
            builder.getClass().getDeclaredMethod("loggerComponent", LoggerComponent.class).invoke(builder, loggerComponent);
            builder.getClass().getDeclaredMethod(compileInput.adapterProjectInput().resourcesComponent().idAsCamelCase(), resourcesComponentClass).invoke(builder, resourceRegistriesProvider);
            builder.getClass().getDeclaredMethod("resourceServiceComponent", ResourceServiceComponent.class).invoke(builder, resourceServiceComponent);
            builder.getClass().getDeclaredMethod("platformComponent", PlatformComponent.class).invoke(builder, platformComponent);
            languageComponent = (LanguageComponent)builder.getClass().getDeclaredMethod("build").invoke(builder);
        }

        final PieComponent pieComponent = DaggerRootPieComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .rootPieModule(rootPieModuleProvider.get()
                .addTaskDefsFrom(languageComponent)
                .withSerdeFactory(__ -> JavaSerde.createWithClassLoaderOverride(classLoader))
            )
            .build();

        return new DynamicLanguage(rootDirectory, compileInput, classLoader, resourceRegistriesProvider, resourceServiceComponent, languageComponent, pieComponent);
    }
}
