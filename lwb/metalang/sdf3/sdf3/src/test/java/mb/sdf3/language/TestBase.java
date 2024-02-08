package mb.sdf3.language;

import mb.common.util.MultiMapView;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.NoopClassLoaderUrlResolver;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3ConstraintAnalyzerFactory;
import mb.sdf3.Sdf3ParserFactory;
import mb.sdf3.stratego.Sdf3StrategoRuntimeBuilderFactory;
import mb.sdf3.Sdf3StylerFactory;
import mb.spoofax.test.SingleBaseLanguageTestBase;

class TestBase extends SingleBaseLanguageTestBase {
    public static final Sdf3ClassLoaderResources classloaderResources = new Sdf3ClassLoaderResources(ClassLoaderResourceRegistry.defaultUrlResolver, ClassLoaderResourceRegistry.defaultToNativeResolver);

    public TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            () -> classloaderResources.resourceRegistry,
            () -> classloaderResources.definitionDirectory,
            (loggerFactory, definitionDirectory) -> new Sdf3ParserFactory(definitionDirectory).create(),
            "Module",
            (loggerFactory, definitionDirectory) -> new Sdf3StylerFactory(loggerFactory, definitionDirectory).create(),
            (loggerFactory, resourceService, definitionDirectory, rootPath) -> {
                return new Sdf3StrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDirectory).create()
                    .addContextObject(new mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext(
                        definitionDirectory.getPath(),
                        MultiMapView.of("sdf3", rootPath),
                        MultiMapView.of()
                    ));
            },
            resourceService -> new Sdf3ConstraintAnalyzerFactory(resourceService).create(),
            true
        );
    }
}
