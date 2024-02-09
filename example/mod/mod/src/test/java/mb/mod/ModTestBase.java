package mb.mod;

import mb.common.util.MultiMapView;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.test.SingleBaseLanguageTestBase;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;

class ModTestBase extends SingleBaseLanguageTestBase {
    public static final ModClassLoaderResources classloaderResources = new ModClassLoaderResources(ClassLoaderResourceRegistry.defaultUrlResolver, ClassLoaderResourceRegistry.defaultToNativeResolver);

    public ModTestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            () -> classloaderResources.resourceRegistry,
            () -> classloaderResources.definitionDirectory,
            (loggerFactory, definitionDirectory) -> new ModParserFactory(definitionDirectory).create(),
            "Start",
            (loggerFactory, definitionDirectory) -> new ModStylerFactory(loggerFactory, definitionDirectory).create(),
            (loggerFactory, resourceService, definitionDirectory, rootPath) -> {
                return new ModStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDirectory).create()
                    .addContextObject(new mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext(
                        definitionDirectory.getPath(),
                        MultiMapView.of("mod", rootPath),
                        MultiMapView.of()
                    ));
            },
            resourceService -> new ModConstraintAnalyzerFactory(resourceService).create(),
            true
        );
    }
}
