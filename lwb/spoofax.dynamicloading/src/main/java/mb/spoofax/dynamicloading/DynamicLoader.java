package mb.spoofax.dynamicloading;

import mb.log.stream.StreamLoggerFactory;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;

public class DynamicLoader {
    public final ClassLoaderResourceRegistry classLoaderResourceRegistry;
    public final PlatformComponent platformComponent;
    public final Spoofax3CompilerStandalone spoofax3CompilerStandalone;

    public DynamicLoader() {
        classLoaderResourceRegistry =
            new ClassLoaderResourceRegistry("spoofax3.standalone", Spoofax3CompilerStandalone.class.getClassLoader());
        platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()))
            .resourceRegistriesModule(new ResourceRegistriesModule(classLoaderResourceRegistry))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .build();
        spoofax3CompilerStandalone = new Spoofax3CompilerStandalone(platformComponent);
    }

    public DynamicLanguageComponent createDynamicLanguageComponent(CompileToJavaClassFiles.Input compilerInput) {
        return new DynamicLanguageComponent(
            spoofax3CompilerStandalone.component.getResourceService(),
            spoofax3CompilerStandalone.component.getPie(),
            platformComponent,
            spoofax3CompilerStandalone.component.getCompileToJavaClassFiles(),
            compilerInput
        );
    }
}
