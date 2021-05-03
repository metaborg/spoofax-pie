package mb.spoofax.lwb.dynamicloading;

import mb.common.util.ExceptionPrinter;
import mb.log.api.LoggerFactory;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.PieBuilder;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.api.Tracer;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.SerializingStore;
import mb.pie.runtime.tracer.CompositeTracer;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.dagger.StandaloneSpoofax3Compiler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax.dynamicloading", TestBase.class.getClassLoader());

    HierarchicalResource temporaryDirectory;
    MetricsTracer metricsTracer;
    LoggerComponent loggerComponent;
    RootResourceServiceComponent rootResourceServiceComponent;
    StandaloneSpoofax3Compiler standaloneSpoofax3Compiler;
    ResourceService resourceService;
    DynamicLoadingComponent dynamicLoadingComponent;
    DynamicLoad dynamicLoad;
    DynamicLanguageRegistry dynamicLanguageRegistry;
    PieComponent pieComponent;

    void setup(Path temporaryDirectoryPath) throws IOException {
        temporaryDirectory = new FSResource(temporaryDirectoryPath);

        final WritableResource pieStore = temporaryDirectory.appendRelativePath("pie.store");
        final PieBuilder.StoreFactory storeFactory = (serde, __, ___) -> new SerializingStore<>(serde, pieStore, InMemoryStore::new, InMemoryStore.class, false);
        metricsTracer = new MetricsTracer();
        final Function<LoggerFactory, Tracer> tracerFactory = loggerFactory -> new CompositeTracer(new LoggingTracer(loggerFactory), metricsTracer);

        loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutVeryVerbose())
            .build();
        rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
            .loggerComponent(loggerComponent)
            .build();
        standaloneSpoofax3Compiler = new StandaloneSpoofax3Compiler(
            loggerComponent,
            rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
            new PieModule(PieBuilderImpl::new)
        );
        resourceService = standaloneSpoofax3Compiler.compiler.resourceServiceComponent.getResourceService();
        dynamicLoadingComponent = DaggerDynamicLoadingComponent.builder()
            .dynamicLoadingPieModule(new DynamicLoadingPieModule(() -> new RootPieModule(PieBuilderImpl::new)
                .withStoreFactory(storeFactory)
                .withTracerFactory(tracerFactory))
            )
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(standaloneSpoofax3Compiler.compiler.resourceServiceComponent)
            .platformComponent(standaloneSpoofax3Compiler.compiler.platformComponent)
            .cfgComponent(standaloneSpoofax3Compiler.compiler.cfgComponent)
            .spoofax3CompilerComponent(standaloneSpoofax3Compiler.compiler.component)
            .build();
        dynamicLoad = dynamicLoadingComponent.getDynamicLoad();
        dynamicLanguageRegistry = dynamicLoadingComponent.getDynamicLanguageRegistry();
        pieComponent = DaggerPieComponent.builder()
            .pieModule(standaloneSpoofax3Compiler.pieComponent.createChildModule(dynamicLoadingComponent))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(standaloneSpoofax3Compiler.compiler.resourceServiceComponent)
            .build();
    }

    void teardown() throws Exception {
        pieComponent.close();
        pieComponent = null;
        dynamicLanguageRegistry = null;
        dynamicLoad = null;
        dynamicLoadingComponent.close();
        dynamicLoadingComponent = null;
        resourceService = null;
        standaloneSpoofax3Compiler.close();
        standaloneSpoofax3Compiler = null;
        rootResourceServiceComponent.close();
        rootResourceServiceComponent = null;
        loggerComponent = null;
        metricsTracer = null;
        temporaryDirectory.close();
        temporaryDirectory = null;
    }


    MixedSession newSession() {
        return pieComponent.newSession();
    }

    Task<OutTransient<DynamicLanguage>> dynamicLoadTask(CompileLanguage.Args args) {
        return dynamicLoad.createTask(args);
    }

    Task<OutTransient<DynamicLanguage>> dynamicLoadTask(ResourcePath rootDirectory) {
        return dynamicLoad.createTask(CompileLanguage.Args.builder().rootDirectory(rootDirectory).build());
    }

    DynamicLanguage requireDynamicLoad(Session session, CompileLanguage.Args args) throws ExecException, InterruptedException {
        return session.require(dynamicLoadTask(args)).getValue();
    }

    DynamicLanguage requireDynamicLoad(Session session, ResourcePath rootDirectory) throws ExecException, InterruptedException {
        return session.require(dynamicLoadTask(rootDirectory)).getValue();
    }

    DynamicLanguage getDynamicLoadOutput(TopDownSession session, CompileLanguage.Args args) {
        return session.getOutput(dynamicLoadTask(args)).getValue();
    }

    DynamicLanguage getDynamicLoadOutput(TopDownSession session, ResourcePath rootDirectory) {
        return session.getOutput(dynamicLoadTask(rootDirectory)).getValue();
    }


    void copyResourcesToTemporaryDirectory(String sourceFilesPath) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(temporaryDirectory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
        }
    }

    void printThrowable(Throwable throwable) {
        final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
        exceptionPrinter.addCurrentDirectoryContext(temporaryDirectory);
        exceptionPrinter.printException(throwable, System.err);
    }
}
