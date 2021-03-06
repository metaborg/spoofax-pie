package mb.spoofax.lwb.dynamicloading;

import mb.common.util.ExceptionPrinter;
import mb.log.api.LoggerFactory;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.PieBuilder;
import mb.pie.api.Tracer;
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
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax.dynamicloading", TestBase.class.getClassLoader());

    HierarchicalResource temporaryDirectory;
    MetricsTracer metricsTracer;
    LoggerComponent loggerComponent;
    ResourceService resourceService;
    DynamicLoader dynamicLoader;

    void setup(Path temporaryDirectoryPath) throws IOException {
        this.temporaryDirectory = new FSResource(temporaryDirectoryPath);

        final WritableResource pieStore = temporaryDirectory.appendRelativePath("pie.store");
        final PieBuilder.StoreFactory storeFactory = (serde, __, ___) -> new SerializingStore<>(serde, pieStore, InMemoryStore::new, InMemoryStore.class, false);
        this.metricsTracer = new MetricsTracer();
        final Function<LoggerFactory, Tracer> tracerFactory = loggerFactory -> new CompositeTracer(new LoggingTracer(loggerFactory), metricsTracer);

        this.loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutErrorsAndWarnings())
            .build();
        final RootResourceServiceComponent rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
            .loggerComponent(loggerComponent)
            .build();
        final Spoofax3Compiler spoofax3Compiler = new Spoofax3Compiler(
            loggerComponent,
            rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
            new PieModule(PieBuilderImpl::new)
        );
        this.resourceService = spoofax3Compiler.resourceServiceComponent.getResourceService();
        this.dynamicLoader = new DynamicLoader(
            spoofax3Compiler,
            () -> new RootPieModule(PieBuilderImpl::new)
                .withStoreFactory(storeFactory)
                .withTracerFactory(tracerFactory)
        );
    }

    void teardown() throws Exception {
        this.dynamicLoader.close();
        this.dynamicLoader = null;
        this.resourceService = null;
        this.metricsTracer = null;
        this.temporaryDirectory = null;
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
