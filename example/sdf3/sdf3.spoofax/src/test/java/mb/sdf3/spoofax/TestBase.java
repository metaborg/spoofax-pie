package mb.sdf3.spoofax;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceService;
import mb.resource.text.TextResourceRegistry;
import mb.sdf3.spoofax.util.DaggerPlatformTestComponent;
import mb.sdf3.spoofax.util.DaggerSdf3TestComponent;
import mb.sdf3.spoofax.util.PlatformTestComponent;
import mb.sdf3.spoofax.util.Sdf3TestComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;

class TestBase {
    final PlatformTestComponent platformComponent = DaggerPlatformTestComponent
        .builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .pieModule(new PieModule(PieBuilderImpl::new))
        .build();
    final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    final Logger log = loggerFactory.create(TestBase.class);
    final ResourceService resourceService = platformComponent.getResourceService();
    final TextResourceRegistry textResourceRegistry = platformComponent.getTextResourceRegistry();

    final Sdf3TestComponent languageComponent = DaggerSdf3TestComponent
        .builder()
        .platformComponent(platformComponent)
        .sdf3Module(new Sdf3Module())
        .build();
    final Sdf3Instance languageInstance = languageComponent.getLanguageInstance();
}
