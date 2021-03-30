package mb.mod;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.resource.classloader.NoopClassLoaderUrlResolver;
import mb.spoofax.test.SingleBaseLanguageTestBase;

class ModTestBase extends SingleBaseLanguageTestBase {
    public static final ModClassLoaderResources classloaderResources = new ModClassLoaderResources(new NoopClassLoaderUrlResolver());

    public ModTestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            () -> classloaderResources.resourceRegistry,
            () -> classloaderResources.definitionDirectory,
            (loggerFactory, definitionDirectory) -> new ModParserFactory(definitionDirectory).create(),
            "Start",
            (loggerFactory, definitionDirectory) -> new ModStylerFactory(loggerFactory, definitionDirectory).create(),
            (loggerFactory, resourceService, definitionDirectory) -> new ModStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDirectory).create(),
            resourceService -> new ModConstraintAnalyzerFactory(resourceService).create(),
            true
        );
    }
}
