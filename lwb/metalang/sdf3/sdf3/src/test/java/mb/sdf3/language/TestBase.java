package mb.sdf3.language;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.resource.classloader.NoopClassLoaderUrlResolver;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3ConstraintAnalyzerFactory;
import mb.sdf3.Sdf3ParserFactory;
import mb.sdf3.Sdf3StrategoRuntimeBuilderFactory;
import mb.sdf3.Sdf3StylerFactory;
import mb.spoofax.test.SingleBaseLanguageTestBase;

class TestBase extends SingleBaseLanguageTestBase {
    public static final Sdf3ClassLoaderResources classloaderResources = new Sdf3ClassLoaderResources(new NoopClassLoaderUrlResolver());

    public TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            () -> classloaderResources.resourceRegistry,
            () -> classloaderResources.definitionDirectory,
            (loggerFactory, definitionDirectory) -> new Sdf3ParserFactory(definitionDirectory).create(),
            "Module",
            (loggerFactory, definitionDirectory) -> new Sdf3StylerFactory(loggerFactory, definitionDirectory).create(),
            (loggerFactory, resourceService, definitionDirectory) -> new Sdf3StrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDirectory).create(),
            resourceService -> new Sdf3ConstraintAnalyzerFactory(resourceService).create(),
            true
        );
    }
}
