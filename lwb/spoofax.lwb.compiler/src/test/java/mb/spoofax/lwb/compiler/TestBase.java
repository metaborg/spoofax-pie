package mb.spoofax.lwb.compiler;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.Pie;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
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

public class TestBase {
    final LoggerComponent loggerComponent = DaggerLoggerComponent.builder()
        .loggerModule(LoggerModule.stdOutVeryVerbose())
        .build();
    final ClassLoaderResourceRegistry classLoaderResourceRegistry =
        new ClassLoaderResourceRegistry("language-compiler", CompileLanguageTest.class.getClassLoader());
    final RootResourceServiceComponent rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
        .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
        .loggerComponent(loggerComponent)
        .build();
    final StandaloneSpoofax3Compiler compiler = new StandaloneSpoofax3Compiler(
        loggerComponent,
        rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
        new PieModule(PieBuilderImpl::new)
    );
    final Pie pie = compiler.pieComponent.getPie();
    final CompileLanguage compileLanguage = compiler.compiler.component.getCompileLanguage();

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
