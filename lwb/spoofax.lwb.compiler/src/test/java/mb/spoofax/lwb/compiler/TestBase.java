package mb.spoofax.lwb.compiler;

import mb.common.util.ExceptionPrinter;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
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
import mb.spoofax.lwb.compiler.dagger.StandaloneSpoofax3Compiler;

import java.io.IOException;
import java.nio.file.Path;

class TestBase {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry =
        new ClassLoaderResourceRegistry("language-compiler", CompileLanguageTest.class.getClassLoader());

    HierarchicalResource rootDirectory;
    ExceptionPrinter exceptionPrinter;
    LoggerComponent loggerComponent;
    RootResourceServiceComponent rootResourceServiceComponent;
    StandaloneSpoofax3Compiler compiler;
    ResourceService resourceService;
    PieComponent pieComponent;
    CompileLanguage compileLanguage;

    void setup(Path temporaryDirectoryPath) throws IOException {
        rootDirectory = new FSResource(temporaryDirectoryPath);
        exceptionPrinter = new ExceptionPrinter().addCurrentDirectoryContext(rootDirectory);
        loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdOutVeryVerbose())
            .build();
        rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
            .loggerComponent(loggerComponent)
            .build();
        compiler = new StandaloneSpoofax3Compiler(
            loggerComponent,
            rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
            new PieModule(PieBuilderImpl::new)
        );
        resourceService = compiler.compiler.resourceServiceComponent.getResourceService();
        pieComponent = compiler.pieComponent;
        compileLanguage = compiler.compiler.component.getCompileLanguage();
    }

    void teardown() throws Exception {
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
}
