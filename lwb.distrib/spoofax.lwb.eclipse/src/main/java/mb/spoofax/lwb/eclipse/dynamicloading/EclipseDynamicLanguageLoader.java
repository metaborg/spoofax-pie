package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipseLifecycleParticipant;
import mb.spoofax.eclipse.LifecycleParticipantManager;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.menu.MenuShared;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentLoader;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingScope;
import mb.spt.eclipse.SptLanguageFactory;
import org.eclipse.core.commands.AbstractHandler;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

@DynamicLoadingScope
public class EclipseDynamicLanguageLoader implements DynamicComponentLoader {
    @Inject public EclipseDynamicLanguageLoader() {} /* Default constructor needed for injection. */

    @Override
    public DynamicComponent load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        Iterable<ResourcePath> classPath
    ) throws Exception {
        if(!compileInput.eclipseProjectInput().isPresent()) {
            throw new RuntimeException("Cannot dynamically load language at '" + rootDirectory + "' because it has no Eclipse project input");
        }
        return load(rootDirectory, compileInput, compileInput.eclipseProjectInput().get(), classPath);
    }

    private DynamicComponent load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        EclipseProjectCompiler.Input eclipseInput,
        Iterable<ResourcePath> classPath
    ) throws Exception {
        final URLClassLoader classLoader = new URLClassLoader(DynamicComponentLoader.classPathToUrl(classPath, SpoofaxPlugin.getBaseResourceServiceComponent().getResourceService()), EclipseDynamicLanguageLoader.class.getClassLoader());
        final Class<?> factoryClass = classLoader.loadClass(eclipseInput.languageFactory().qualifiedId());
        final Method getLanguageMethod = factoryClass.getDeclaredMethod("getLanguage");
        final EclipseLifecycleParticipant participant = (EclipseLifecycleParticipant)getLanguageMethod.invoke(null);
        final LifecycleParticipantManager.DynamicGroup group = SpoofaxPlugin.getStaticComponentManager().registerDynamic(
            rootDirectory,
            participant,
            // HACK: add SPT test case resource registry as an additional resource registry to enable SPT testing
            SptLanguageFactory.getLanguage().getResourcesComponent().getTestCaseResourceRegistry()
        );

        final MenuShared resourceContextMenu = (MenuShared)classLoader.loadClass(eclipseInput.resourceContextMenu().qualifiedId()).getDeclaredConstructor().newInstance();
        final MenuShared editorContextMenu = (MenuShared)classLoader.loadClass(eclipseInput.editorContextMenu().qualifiedId()).getDeclaredConstructor().newInstance();
        final MenuShared mainMenu = (MenuShared)classLoader.loadClass(eclipseInput.mainMenu().qualifiedId()).getDeclaredConstructor().newInstance();
        final AbstractHandler runCommandHandler = (AbstractHandler)classLoader.loadClass(eclipseInput.runCommandHandler().qualifiedId()).getDeclaredConstructor().newInstance();

        return new EclipseDynamicLanguage(
            rootDirectory,
            compileInput,
            classLoader,
            group.resourceRegistriesProvider,
            group.resourceServiceComponent,
            (EclipseLanguageComponent)group.languageComponent,
            group.pieComponent,
            resourceContextMenu,
            editorContextMenu,
            mainMenu,
            runCommandHandler
        );
    }
}
