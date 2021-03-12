package mb.spoofax.lwb.compiler;

import mb.common.util.ExceptionPrinter;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
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
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class CompileLanguageWithCfgToJavaClassPathTest {
    final LoggerComponent loggerComponent = DaggerLoggerComponent.builder()
        .loggerModule(LoggerModule.stdOutVeryVerbose())
        .build();
    final ClassLoaderResourceRegistry classLoaderResourceRegistry =
        new ClassLoaderResourceRegistry("language-compiler", CompileLanguageWithCfgToJavaClassPathTest.class.getClassLoader());
    final RootResourceServiceComponent rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
        .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
        .loggerComponent(loggerComponent)
        .build();
    final Spoofax3Compiler compiler = new Spoofax3Compiler(
        loggerComponent,
        rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
        new PieModule(PieBuilderImpl::new)
    );
    final Pie pie = compiler.pieComponent.getPie();
    final CompileLanguageWithCfgToJavaClassPath compileLanguageWithCfgToJavaClassPath = compiler.component.getCompileLanguageWithCfgToJavaClassPath();

    @Test void testCompileCharsLanguage(@TempDir Path temporaryDirectoryPath) throws Exception {
        // Copy language specification sources to the temporary directory.
        final FSResource temporaryDirectory = new FSResource(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/compiler/chars", temporaryDirectory);
        try(final MixedSession session = pie.newSession()) {
            session.require(compileLanguageWithCfgToJavaClassPath.createTask(temporaryDirectory.getPath())).unwrap();
        } catch(CompileLanguageWithCfgToJavaClassPathException e) {
            final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();
            exceptionPrinter.addCurrentDirectoryContext(temporaryDirectory);
            System.err.println(exceptionPrinter.printExceptionToString(e));
            throw e;
        }
    }


    void copyResourcesToTemporaryDirectory(String sourceFilesPath, HierarchicalResource temporaryDirectory) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(temporaryDirectory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
        }
    }
}
