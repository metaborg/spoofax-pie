package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.common.util.SetView;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.MixedSession;
import mb.pie.api.OutTransient;
import mb.pie.api.Session;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
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
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.component.StaticComponentManagerBuilder;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompiler;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinitionException;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spt.SptComponent;
import mb.spt.SptParticipant;
import mb.spt.dynamicloading.DynamicLanguageUnderTestProvider;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax.dynamicloading", TestBase.class.getClassLoader());

    HierarchicalResource temporaryDirectory;
    HierarchicalResource rootDirectory;
    ExceptionPrinter exceptionPrinter;

    LoggerComponent loggerComponent;
    RootResourceServiceComponent baseResourceServiceComponent;
    PlatformComponent platformComponent;

    SerializingStoreInMemoryBuffer spoofax3CompilerStoreBuffer;
    SpoofaxLwbCompiler spoofaxLwbCompiler;
    CfgRootDirectoryToObject cfgRootDirectoryToObject;
    CompileLanguageDefinition compileLanguageDefinition;
    SptComponent sptComponent;
    ResourceService resourceService;
    MetricsTracer languageMetricsTracer;
    DynamicLoadingComponent dynamicLoadingComponent;
    DynamicComponentManager dynamicComponentManager;
    DynamicLoad dynamicLoad;
    PieComponent pieComponent;

    void setup(Path temporaryDirectoryPath) throws IOException {
        temporaryDirectory = new FSResource(temporaryDirectoryPath);
        rootDirectory = temporaryDirectory.appendRelativePath("language").createDirectory();
        exceptionPrinter = new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory);

        loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutVeryVerbose())
            .build();
        baseResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .loggerComponent(loggerComponent)
            .build();
        platformComponent = DaggerPlatformComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(baseResourceServiceComponent)
            .build();

        final StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder =
            new StaticComponentManagerBuilder<>(loggerComponent, baseResourceServiceComponent, platformComponent, PieBuilderImpl::new);
        SpoofaxLwbCompiler.registerParticipants(builder);
        builder.registerParticipant(new SptParticipant<>()); // Also register SPT. TODO: should the LWB compiler do this?
        spoofax3CompilerStoreBuffer = new SerializingStoreInMemoryBuffer();
        builder.registerPieModuleCustomizer(pieModule -> {
            pieModule.withSerdeFactory(loggerFactory -> new FstSerde()); // Use FstSerde for faster serialization
        });
        builder.registerPieModuleCustomizer(pieModule -> {
            pieModule.withStoreFactory((serde, resourceService, loggerFactory) -> SerializingStoreBuilder.ofInMemoryStore(serde)
                .withInMemoryBuffer(spoofax3CompilerStoreBuffer) // Serialize/deserialize compiler tasks with memory buffer.
                .withLoggingDeserializeFailHandler(loggerFactory)
                .build()
            );
        }, "mb.spoofax.lwb");
        final StaticComponentManager staticComponentManager = builder.build();
        spoofaxLwbCompiler = SpoofaxLwbCompiler.fromComponentManager(staticComponentManager);

        cfgRootDirectoryToObject = spoofaxLwbCompiler.spoofaxLwbCompilerComponent.getCfgComponent().getCfgRootDirectoryToObject();
        compileLanguageDefinition = spoofaxLwbCompiler.spoofaxLwbCompilerComponent.getCompileLanguageDefinition();
        sptComponent = spoofaxLwbCompiler.componentManager.getOneSubcomponent(SptComponent.class).unwrap();
        resourceService = spoofaxLwbCompiler.resourceServiceComponent.getResourceService();
        languageMetricsTracer = new MetricsTracer();
        dynamicLoadingComponent = DaggerDynamicLoadingComponent.builder()
            .dynamicLoadingModule(new DynamicLoadingModule(
                PieBuilderImpl::new,
                (loggerFactory, classLoader) -> new FstSerde(classLoader)) // Use FstSerde for faster serialization
                .addPieModuleCustomizers(pieModule -> {
                    // Use languageMetricsTracer to support checking which tasks have been executed.
                    pieModule.withTracerFactory(loggerFactory -> new CompositeTracer(languageMetricsTracer, new LoggingTracer(loggerFactory)));
                })
                .withBaseComponentManager(staticComponentManager)
            )
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(baseResourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        dynamicComponentManager = dynamicLoadingComponent.getDynamicComponentManager();
        dynamicLoad = dynamicLoadingComponent.getDynamicLoad();

        sptComponent.getLanguageUnderTestProviderWrapper().set(new DynamicLanguageUnderTestProvider(
            dynamicComponentManager,
            dynamicLoad,
            // NOTE: this has to be the same supplier as being used by the rest of the tests, otherwise the dynamic
            //       components will be reloaded by SPT, which closes and thus invalidates previous dynamic components.
            this::dynamicLoadSupplierOutputSupplier
        ));

        // TODO: this won't work properly with bottom-up building when builds are executed from `standaloneSpoofax3Compiler.pieComponent`.
        pieComponent = DaggerPieComponent.builder()
            .pieModule(spoofaxLwbCompiler.pieComponent.createChildModule(dynamicLoadingComponent))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(spoofaxLwbCompiler.resourceServiceComponent)
            .build();
    }

    void teardown() throws Exception {
        exceptionPrinter = null;
        pieComponent.close();
        pieComponent = null;
        dynamicLoad = null;
        dynamicComponentManager = null;
        dynamicLoadingComponent.close();
        dynamicLoadingComponent = null;
        languageMetricsTracer = null;
        resourceService = null;
        sptComponent.close();
        sptComponent = null;
        spoofax3CompilerStoreBuffer.close();
        spoofax3CompilerStoreBuffer = null;
        compileLanguageDefinition = null;
        cfgRootDirectoryToObject = null;
        spoofaxLwbCompiler.close();
        spoofaxLwbCompiler = null;
        baseResourceServiceComponent.close();
        baseResourceServiceComponent = null;
        loggerComponent = null;
        rootDirectory.close();
        rootDirectory = null;
        temporaryDirectory.close();
        temporaryDirectory = null;
    }


    static CompileLanguageDefinition.Args compileLanguageArgs(ResourcePath rootDirectory) {
        return CompileLanguageDefinition.Args.builder().rootDirectory(rootDirectory).build();
    }


    MixedSession newSession() {
        return pieComponent.newSession();
    }

    Task<Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException>> compileLanguageTask(ResourcePath rootDirectory) {
        return compileLanguageDefinition.createTask(compileLanguageArgs(rootDirectory));
    }

    Supplier<Result<DynamicLoad.SupplierOutput, ?>> dynamicLoadSupplierOutputSupplier(ResourcePath rootDirectory) {
        return compileLanguageTask(rootDirectory).toSupplier().map(new ToDynamicLoadSupplierOutput());
    }

    Task<OutTransient<Result<DynamicComponent, DynamicLoadException>>> dynamicLoadTask(ResourcePath rootDirectory) {
        return dynamicLoad.createTask(dynamicLoadSupplierOutputSupplier(rootDirectory));
    }

    DynamicComponent requireDynamicLoad(Session session, ResourcePath rootDirectory) throws Exception {
        return session.require(dynamicLoadTask(rootDirectory)).getValue().unwrap();
    }

    CompileLanguageInput requireCompileLanguageInput(Session session, ResourcePath rootDirectory) throws Exception {
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result = session.require(cfgRootDirectoryToObject.createTask(rootDirectory));
        return result.expect("Getting configuration for '" + rootDirectory + "' to succeed, but it failed").compileLanguageInput;
    }

    DynamicComponent getDynamicLoadOutput(TopDownSession session, ResourcePath rootDirectory) throws Exception {
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


    private static class ToDynamicLoadSupplierOutput extends StatelessSerializableFunction<Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException>, Result<DynamicLoad.SupplierOutput, ?>> {
        @Override
        public Result<DynamicLoad.SupplierOutput, ?> apply(Result<CompileLanguageDefinition.Output, CompileLanguageDefinitionException> r) {
            return r.map(o -> new DynamicLoad.SupplierOutput(o.rootDirectory(), SetView.of(o.javaClassPaths()), o.participantClassQualifiedId()));
        }
    }
}
