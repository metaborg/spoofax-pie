package mb.spoofax.dynamicloading;

import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDefs;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.dynamicloading.task.DynamicStyle;
import mb.spoofax.dynamicloading.task.ReloadLanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

public class DynamicLanguageComponent implements LanguageComponent, AutoCloseable {
    private final ResourceService parentResourceService;
    private final PlatformComponent platformComponent;
    private final CompileToJavaClassFiles.Input compilerInput;

    private final DynamicResourceService resourceService; // TODO: do not recreate, just like PIE?
    private final Pie pie;
    private final DynamicTaskDefs dynamicTaskDefs;
    private final DynamicLanguageInstance languageInstance;
    private final DynamicUrlClassLoader classLoader;


    public DynamicLanguageComponent(
        ResourceService parentResourceService,
        Pie parentPie,
        PlatformComponent platformComponent,
        CompileToJavaClassFiles compiler,
        CompileToJavaClassFiles.Input compilerInput // TODO: must be made dynamic
    ) {
        this.parentResourceService = parentResourceService;
        this.platformComponent = platformComponent;
        this.compilerInput = compilerInput;

        this.resourceService = new DynamicResourceService(parentResourceService);
        final ReloadLanguageComponent reloadLanguageComponent = new ReloadLanguageComponent(this, compiler, compilerInput);
        final DynamicStyle dynamicStyle = new DynamicStyle(reloadLanguageComponent);
        final TaskDefs additionalTaskDefs = new MapTaskDefs(
            reloadLanguageComponent,
            dynamicStyle
        );
        this.dynamicTaskDefs = new DynamicTaskDefs();
        this.pie = parentPie.createChildBuilder()
            .withResourceService(resourceService)
            .addTaskDefs(additionalTaskDefs)
            .addTaskDefs(dynamicTaskDefs)
            .build();
        final Shared shared = compilerInput.languageProjectInput().shared();
        this.languageInstance = new DynamicLanguageInstance(shared.name(), SetView.copyOf(shared.fileExtensions()), dynamicStyle);
        this.classLoader = new DynamicUrlClassLoader(DynamicLanguageComponent.class.getClassLoader());
    }

    @Override public void close() throws IOException {
        pie.close();
        classLoader.close();
    }


    @Override public ResourceService getResourceService() {
        return resourceService;
    }

    @Override public Pie getPie() {
        return pie;
    }

    @Override public TaskDefs getTaskDefs() {
        return dynamicTaskDefs;
    }

    @Override public LanguageInstance getLanguageInstance() {
        return languageInstance;
    }


    public LanguageComponent reload(ListView<ResourcePath> classPath) throws ReflectiveOperationException, IOException {
        final ArrayList<URL> urls = new ArrayList<>();
        for(ResourcePath path : classPath) {
            final @Nullable File file = resourceService.toLocalFile(path);
            if(file == null) {
                throw new IOException("Cannot dynamically load classes or resources from '" + path + "', it is not a directory on the local file system");
            }
            urls.add(file.toURI().toURL());
        }
        this.classLoader.reload(urls.toArray(new URL[0]), DynamicLanguageComponent.class.getClassLoader());
        final Class<?> daggerComponentClass = classLoader.loadClass(compilerInput.adapterProjectInput().daggerComponent().qualifiedId());
        final Method builderMethod = daggerComponentClass.getDeclaredMethod("builder");
        final Object builder = builderMethod.invoke(null);
        builder.getClass().getDeclaredMethod("platformComponent", PlatformComponent.class).invoke(builder, platformComponent);
        final LanguageComponent languageComponent = (LanguageComponent)builder.getClass().getDeclaredMethod("build").invoke(builder);
        this.resourceService.setResourceService(parentResourceService.createChild(languageComponent.getResourceService()));
        this.dynamicTaskDefs.setTaskDefs(languageComponent.getTaskDefs());
        return languageComponent;
    }
}
