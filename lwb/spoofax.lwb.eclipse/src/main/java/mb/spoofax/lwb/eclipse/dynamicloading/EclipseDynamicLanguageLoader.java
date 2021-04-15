package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.eclipse.EclipseLifecycleParticipant;
import mb.spoofax.eclipse.LifecycleParticipantManager;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageLoader;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingScope;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;

@DynamicLoadingScope
public class EclipseDynamicLanguageLoader implements DynamicLanguageLoader {
    @Inject public EclipseDynamicLanguageLoader() {} /* Default constructor needed for injection. */

    @Override
    public DynamicLanguage load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        List<ResourcePath> classPath
    ) throws Exception {
        if(!compileInput.eclipseProjectInput().isPresent()) {
            throw new RuntimeException("Cannot dynamically load language at '" + rootDirectory + "' because it has no Eclipse project input");
        }
        return load(rootDirectory, compileInput, compileInput.eclipseProjectInput().get(), classPath);
    }

    private DynamicLanguage load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        EclipseProjectCompiler.Input eclipseInput,
        List<ResourcePath> classPath
    ) throws Exception {
        final URLClassLoader classLoader = new URLClassLoader(DynamicLanguageLoader.classPathToUrl(classPath, SpoofaxPlugin.getBaseResourceServiceComponent().getResourceService()), EclipseDynamicLanguageLoader.class.getClassLoader());
        final Class<?> factoryClass = classLoader.loadClass(eclipseInput.languageFactory().qualifiedId());
        final Method getLanguageMethod = factoryClass.getDeclaredMethod("getLanguage");
        final EclipseLifecycleParticipant participant = (EclipseLifecycleParticipant)getLanguageMethod.invoke(null);
        final LifecycleParticipantManager.DynamicGroup group = SpoofaxPlugin.getLifecycleParticipantManager().registerDynamic(rootDirectory, participant);
        return new DynamicLanguage(rootDirectory, compileInput, classLoader, group.resourceRegistriesProvider, group.resourceServiceComponent, group.languageComponent, group.pieComponent);
    }
}
