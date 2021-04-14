package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
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
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

@DynamicLoadingScope
public class DynamicLoad implements TaskDef<CompileLanguage.Args, OutTransient<DynamicLanguage>> {
    private final LoggerComponent loggerComponent;
    private final ResourceServiceComponent resourceServiceComponent;
    private final PlatformComponent platformComponent;
    private final DynamicLanguageRegistry dynamicLanguageRegistry;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final CompileLanguage compileLanguage;
    private final Provider<RootPieModule> rootPieModuleProvider;

    @Inject public DynamicLoad(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        DynamicLanguageRegistry dynamicLanguageRegistry,
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        CompileLanguage compileLanguage,
        Provider<RootPieModule> rootPieModuleProvider
    ) {
        this.loggerComponent = loggerComponent;
        this.resourceServiceComponent = resourceServiceComponent;
        this.platformComponent = platformComponent;
        this.dynamicLanguageRegistry = dynamicLanguageRegistry;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.compileLanguage = compileLanguage;
        this.rootPieModuleProvider = rootPieModuleProvider;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public OutTransient<DynamicLanguage> exec(ExecContext context, CompileLanguage.Args args) throws Exception {
        final Result<CompileLanguage.Output, CompileLanguageException> result = context.require(compileLanguage, args);
        final CompileLanguage.Output output = result.unwrap(); // TODO: properly handle error
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
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> cfgResult = context.require(cfgRootDirectoryToObject, args.rootDirectory());
        final CompileLanguageInput compileInput = cfgResult.unwrap().compileLanguageInput; // TODO: properly handle error.
        final DynamicLanguage dynamicLanguage = new DynamicLanguage(
            args.rootDirectory(),
            compileInput,
            classPath.toArray(new URL[0]),
            loggerComponent,
            resourceServiceComponent,
            platformComponent,
            rootPieModuleProvider.get()
        );
        dynamicLanguageRegistry.reload(args.rootDirectory(), dynamicLanguage);
        return new OutTransientImpl<>(dynamicLanguage, true);
    }
}
