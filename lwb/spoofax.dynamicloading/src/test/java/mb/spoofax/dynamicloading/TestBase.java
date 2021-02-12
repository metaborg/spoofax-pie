package mb.spoofax.dynamicloading;

import mb.log.api.LoggerFactory;
import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.PieBuilder;
import mb.pie.api.Tracer;
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
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3Compiler;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceServiceComponent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax.dynamicloading", TestBase.class.getClassLoader());

    HierarchicalResource temporaryDirectory;
    MetricsTracer metricsTracer;
    ResourceServiceComponent resourceServiceComponent;
    PlatformComponent platformComponent;
    ResourceService resourceService;
    DynamicLoader dynamicLoader;

    void setup(Path temporaryDirectoryPath) throws IOException {
        this.temporaryDirectory = new FSResource(temporaryDirectoryPath);
        final WritableResource pieStore = temporaryDirectory.appendRelativePath("pie.store");
        final PieBuilder.StoreFactory storeFactory = (serde, __, ___) -> new SerializingStore<>(serde, pieStore, InMemoryStore::new, InMemoryStore.class, false);
        this.metricsTracer = new MetricsTracer();
        final Function<LoggerFactory, Tracer> tracerFactory = loggerFactory -> new CompositeTracer(new LoggingTracer(loggerFactory), metricsTracer);
        final BaseResourceServiceComponent baseResourceServiceComponent = DaggerBaseResourceServiceComponent.create();
        final Spoofax3Compiler spoofax3Compiler = new Spoofax3Compiler(
            baseResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
            new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()),
            new PlatformPieModule(PieBuilderImpl::new, storeFactory, tracerFactory)
        );
        this.resourceServiceComponent = spoofax3Compiler.resourceServiceComponent;
        this.platformComponent = spoofax3Compiler.platformComponent;
        this.resourceService = resourceServiceComponent.getResourceService();
        this.dynamicLoader = new DynamicLoader(new Spoofax3CompilerStandalone(spoofax3Compiler));
    }

    void teardown() throws Exception {
        this.dynamicLoader.close();
        this.dynamicLoader = null;
        this.resourceService = null;
        this.platformComponent = null;
        this.resourceServiceComponent = null;
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

    void logException(Exception e) {
        if(e.getCause() instanceof CompileToJavaClassFiles.CompileException) {
            final CompileToJavaClassFiles.CompileException compilerException = (CompileToJavaClassFiles.CompileException)e.getCause();
            System.err.println(compilerException.getMessage());
            compilerException.getSubMessage().ifPresent(System.err::println);
            compilerException.getSubMessages().ifPresent(System.err::println);
        }
    }
}
