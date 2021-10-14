package mb.spoofax.lwb.dynamicloading;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.SerializingStoreBuilder;
import mb.pie.runtime.store.SerializingStoreInMemoryBuffer;
import mb.pie.runtime.tracer.CompositeTracer;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.pie.serde.fst.FstSerde;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.ResourceService;
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
import mb.spt.DaggerSptComponent;
import mb.spt.DaggerSptResourcesComponent;
import mb.spt.SptComponent;
import mb.spt.SptResourcesComponent;
import mb.spt.dynamicloading.DynamicLanguageUnderTestProvider;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax.dynamicloading", TestBase.class.getClassLoader());

    HierarchicalResource temporaryDirectory;
    HierarchicalResource rootDirectory;
    LoggerComponent loggerComponent;
    SptResourcesComponent sptResourcesComponent;
    RootResourceServiceComponent rootResourceServiceComponent;
    SerializingStoreInMemoryBuffer spoofax3CompilerStoreBuffer;
    StandaloneSpoofax3Compiler standaloneSpoofax3Compiler;
    SptComponent sptComponent;
    ResourceService resourceService;
    MetricsTracer languageMetricsTracer;
    SerializingStoreInMemoryBuffer dynamicLoadingStoreBuffer;
    DynamicLoadingComponent dynamicLoadingComponent;
    DynamicLoad dynamicLoad;
    DynamicLanguageRegistry dynamicLanguageRegistry;
    PieComponent pieComponent;
    ExceptionPrinter exceptionPrinter;

    void setup(Path temporaryDirectoryPath) throws IOException {
        temporaryDirectory = new FSResource(temporaryDirectoryPath);
        rootDirectory = temporaryDirectory.appendRelativePath("language").createDirectory();
        exceptionPrinter = new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory);

        loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutVeryVerbose())
            .build();
        sptResourcesComponent = DaggerSptResourcesComponent.create();
        rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry).addRegistriesFrom(sptResourcesComponent))
            .loggerComponent(loggerComponent)
            .build();
        spoofax3CompilerStoreBuffer = new SerializingStoreInMemoryBuffer();
        standaloneSpoofax3Compiler = new StandaloneSpoofax3Compiler(
            loggerComponent,
            rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
            new PieModule(PieBuilderImpl::new)
                .withSerdeFactory(loggerFactory -> new FstSerde())
                .withStoreFactory((serde, resourceService, loggerFactory) -> SerializingStoreBuilder.ofInMemoryStore(serde)
                    .withInMemoryBuffer(spoofax3CompilerStoreBuffer)
                    .withLoggingDeserializeFailHandler(loggerFactory)
                    .build()
                )
        );
        sptComponent = DaggerSptComponent.builder()
            .loggerComponent(loggerComponent)
            .sptResourcesComponent(sptResourcesComponent)
            .resourceServiceComponent(standaloneSpoofax3Compiler.compiler.resourceServiceComponent)
            .platformComponent(standaloneSpoofax3Compiler.compiler.platformComponent)
            .build();
        resourceService = standaloneSpoofax3Compiler.compiler.resourceServiceComponent.getResourceService();

        languageMetricsTracer = new MetricsTracer();
        dynamicLoadingStoreBuffer = new SerializingStoreInMemoryBuffer();
        dynamicLoadingComponent = DaggerDynamicLoadingComponent.builder()
            .dynamicLoadingPieModule(new DynamicLoadingPieModule(() -> new RootPieModule(PieBuilderImpl::new)
                .withSerdeFactory(loggerFactory -> new FstSerde())
                .withStoreFactory((serde, resourceService, loggerFactory) -> SerializingStoreBuilder.ofInMemoryStore(serde)
                    .withInMemoryBuffer(dynamicLoadingStoreBuffer)
                    .withLoggingDeserializeFailHandler(loggerFactory)
                    .build()
                )
                .withTracerFactory(loggerFactory -> new CompositeTracer(new LoggingTracer(loggerFactory), languageMetricsTracer)))
            )
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(standaloneSpoofax3Compiler.compiler.resourceServiceComponent)
            .platformComponent(standaloneSpoofax3Compiler.compiler.platformComponent)
            .cfgComponent(standaloneSpoofax3Compiler.compiler.cfgComponent)
            .spoofax3CompilerComponent(standaloneSpoofax3Compiler.compiler.component)
            .build();
        dynamicLoad = dynamicLoadingComponent.getDynamicLoad();
        dynamicLanguageRegistry = dynamicLoadingComponent.getDynamicLanguageRegistry();

        sptComponent.getLanguageUnderTestProviderWrapper().set(new DynamicLanguageUnderTestProvider(
            dynamicLanguageRegistry,
            dynamicLoad,
            TestBase::compileLanguageArgs
        ));

        pieComponent = DaggerPieComponent.builder()
            .pieModule(standaloneSpoofax3Compiler.pieComponent.createChildModule(dynamicLoadingComponent, sptComponent))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(standaloneSpoofax3Compiler.compiler.resourceServiceComponent)
            .build();
    }

    void teardown() throws Exception {
        exceptionPrinter = null;
        pieComponent.close();
        pieComponent = null;
        dynamicLanguageRegistry = null;
        dynamicLoad = null;
        dynamicLoadingStoreBuffer.close();
        dynamicLoadingStoreBuffer = null;
        dynamicLoadingComponent.close();
        dynamicLoadingComponent = null;
        languageMetricsTracer = null;
        resourceService = null;
        sptComponent.close();
        sptComponent = null;
        spoofax3CompilerStoreBuffer.close();
        spoofax3CompilerStoreBuffer = null;
        standaloneSpoofax3Compiler.close();
        standaloneSpoofax3Compiler = null;
        rootResourceServiceComponent.close();
        rootResourceServiceComponent = null;
        sptResourcesComponent.close();
        sptResourcesComponent = null;
        loggerComponent = null;
        rootDirectory.close();
        rootDirectory = null;
        temporaryDirectory.close();
        temporaryDirectory = null;
    }


    static CompileLanguage.Args compileLanguageArgs(ResourcePath rootDirectory) {
        return CompileLanguage.Args.builder().rootDirectory(rootDirectory).build();
    }


    MixedSession newSession() {
        return pieComponent.newSession();
    }

    Task<OutTransient<Result<DynamicLanguage, ?>>> dynamicLoadTask(ResourcePath rootDirectory) {
        return dynamicLoad.createTask(compileLanguageArgs(rootDirectory));
    }

    DynamicLanguage requireDynamicLoad(Session session, ResourcePath rootDirectory) throws Exception {
        return session.require(dynamicLoadTask(rootDirectory)).getValue().unwrap();
    }

    DynamicLanguage getDynamicLoadOutput(TopDownSession session, ResourcePath rootDirectory) throws Exception {
        return session.getOutputOrRequireAndEnsureExplicitlyObserved(dynamicLoadTask(rootDirectory)).getValue().unwrap();
    }

    Task<KeyedMessages> sptCheckTask(ResourcePath rootDirectory) {
        return sptComponent.getLanguageInstance().createCheckTask(rootDirectory);
    }

    KeyedMessages requireSptCheck(Session session, ResourcePath rootDirectory) throws Exception {
        return session.require(sptCheckTask(rootDirectory));
    }

    KeyedMessages getSptCheckOutput(TopDownSession session, ResourcePath rootDirectory) throws Exception {
        return session.getOutputOrRequireAndEnsureExplicitlyObserved(sptCheckTask(rootDirectory));
    }


    void copyResourcesToTemporaryDirectory(String sourceFilesPath) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations<FSResource> locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(rootDirectory);
        }
        for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, rootDirectory, false, false);
        }
    }

    void printThrowable(Throwable throwable) {
        exceptionPrinter.printException(throwable, System.err);
    }


    protected void assertNoErrors(KeyedMessages messages) {
        assertNoErrors(messages, "no errors, but found errors");
    }

    protected void assertNoErrors(KeyedMessages messages, String failure) {
        assertFalse(messages.containsError(), () -> "Expected " + failure + ".\n" + exceptionPrinter.printMessagesToString(messages.filter(Message::isErrorOrHigher)));
    }
}
