package mb.spoofax.lwb.compiler;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.util.ExceptionPrinter;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.SerializingStore;
import mb.pie.serde.fst.FstSerde;
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
import mb.spoofax.lwb.compiler.dagger.StandaloneSpoofax3Compiler;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry =
        new ClassLoaderResourceRegistry("language-compiler", CompileLanguageTest.class.getClassLoader());

    HierarchicalResource tempDirectory;
    HierarchicalResource rootDirectory;
    ExceptionPrinter exceptionPrinter;
    LoggerComponent loggerComponent;
    RootResourceServiceComponent rootResourceServiceComponent;
    StandaloneSpoofax3Compiler compiler;
    ResourceService resourceService;
    PieComponent pieComponent;
    CheckLanguageSpecification checkLanguageSpecification;
    CompileLanguage compileLanguage;

    void setup(Path temporaryDirectoryPath) throws IOException {
        tempDirectory = new FSResource(temporaryDirectoryPath);
        rootDirectory = tempDirectory.appendRelativePath("language").ensureDirectoryExists();
        exceptionPrinter = new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory);
        loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutVeryVerbose())
            .build();
        rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
            .loggerComponent(loggerComponent)
            .build();
        recreateCompiler();
    }

    void teardown() throws Exception {
        compileLanguage = null;
        pieComponent.close();
        pieComponent = null;
        resourceService = null;
        compiler.close();
        compiler = null;
        rootResourceServiceComponent.close();
        rootResourceServiceComponent = null;
        loggerComponent = null;
        exceptionPrinter = null;
        rootDirectory.close();
        rootDirectory = null;
    }

    void copyResourcesToTemporaryDirectory(String sourceFilesPath) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(rootDirectory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, rootDirectory, false, false);
        }
    }

    void checkAndCompileLanguage() throws CompileLanguageException, ExecException, InterruptedException {
        final CompileLanguage.Args args = CompileLanguage.Args.builder()
            .rootDirectory(rootDirectory.getPath())
            .build();
        try(final MixedSession session = pieComponent.getPie().newSession()) {
            final KeyedMessages messages = session.require(checkLanguageSpecification.createTask(rootDirectory.getPath()));
            assertNoErrors(messages);
            session.require(compileLanguage.createTask(args)).unwrap();
        } catch(CompileLanguageException e) {
            System.err.println(exceptionPrinter.printExceptionToString(e));
            throw e;
        }
    }

    void recreateCompiler() throws IOException {
        compileLanguage = null;
        checkLanguageSpecification = null;
        pieComponent = null;
        resourceService = null;
        if(compiler != null) {
            compiler.close();
            compiler = null;
        }

        final WritableResource pieStoreFile = tempDirectory.appendRelativePath(".build/compiler.piestore").createParents();
        compiler = new StandaloneSpoofax3Compiler(
            loggerComponent,
            rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
            new PieModule(PieBuilderImpl::new)
                .withSerdeFactory((loggerFactory -> new FstSerde()))
                .withStoreFactory(((serde, resourceService, loggerFactory) -> new SerializingStore<>(
                    serde,
                    pieStoreFile,
                    InMemoryStore::new,
                    InMemoryStore.class,
                    e -> { throw new RuntimeException("Serializing store failed", e); },
                    e -> { throw new RuntimeException("Deserializing store failed", e); }
                )))
        );
        resourceService = compiler.compiler.resourceServiceComponent.getResourceService();
        pieComponent = compiler.pieComponent;
        compileLanguage = compiler.compiler.component.getCompileLanguage();
        checkLanguageSpecification = compiler.compiler.component.getCheckLanguageSpecification();
    }

    void assertNoErrors(KeyedMessages messages) {
        assertNoErrors(messages, "no errors, but found errors");
    }

    void assertNoErrors(KeyedMessages messages, String failure) {
        assertFalse(messages.containsError(), () -> "Expected " + failure + ".\n" + exceptionPrinter.printMessagesToString(messages.filter(Message::isErrorOrHigher)));
    }
}
