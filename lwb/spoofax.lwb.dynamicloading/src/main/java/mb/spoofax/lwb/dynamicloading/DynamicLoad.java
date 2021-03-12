package mb.spoofax.lwb.dynamicloading;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.result.Result;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.ExecContext;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.CompileLanguageToJavaClassPath;
import mb.spoofax.lwb.compiler.CompileLanguageToJavaClassPathException;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageToJavaClassPathInput;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class DynamicLoad implements TaskDef<ResourcePath, OutTransient<DynamicLanguage>> {
    private final LoggerComponent loggerComponent;
    private final ResourceServiceComponent resourceServiceComponent;
    private final PlatformComponent platformComponent;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final CompileLanguageToJavaClassPath compileLanguageToJavaClassPath;
    private final DynamicLoader dynamicLoader;
    private final Provider<RootPieModule> basePieModuleProvider;

    public DynamicLoad(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        CompileLanguageToJavaClassPath compileLanguageToJavaClassPath,
        DynamicLoader dynamicLoader,
        Provider<RootPieModule> basePieModuleProvider
    ) {
        this.loggerComponent = loggerComponent;
        this.resourceServiceComponent = resourceServiceComponent;
        this.platformComponent = platformComponent;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.compileLanguageToJavaClassPath = compileLanguageToJavaClassPath;
        this.dynamicLoader = dynamicLoader;
        this.basePieModuleProvider = basePieModuleProvider;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public OutTransient<DynamicLanguage> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> cfgResult = context.require(cfgRootDirectoryToObject, rootDirectory);
        final CompileLanguageToJavaClassPathInput compileInput = cfgResult.unwrap().compileLanguageToJavaClassPathInput; // TODO: properly handle error.
        final Result<CompileLanguageToJavaClassPath.Output, CompileLanguageToJavaClassPathException> result = context.require(compileLanguageToJavaClassPath, compileInput);
        final CompileLanguageToJavaClassPath.Output output = result.unwrap(); // TODO: properly handle error
        final ArrayList<URL> classPath = new ArrayList<>();
        for(ResourcePath path : output.classPath()) {
            // TODO: properly handle error
            context.require(path, ResourceStampers.hashDirRec(new TrueResourceWalker(), new FileResourceMatcher()));
            final @Nullable File file = context.getResourceService().toLocalFile(path);
            if(file == null) {
                // TODO: properly handle error
                throw new IOException("Cannot dynamically load language; resource at path '" + path + "' is not on the local filesystem, and can therefore not be loaded into a URLClassLoader");
            }
            classPath.add(file.toURI().toURL());
        }
        final DynamicLanguage dynamicLanguage = new DynamicLanguage(
            compileInput,
            classPath.toArray(new URL[0]),
            loggerComponent,
            resourceServiceComponent,
            platformComponent,
            basePieModuleProvider.get()
        );
        dynamicLoader.register(rootDirectory, dynamicLanguage);
        return new OutTransientImpl<>(dynamicLanguage, true);
    }
}
