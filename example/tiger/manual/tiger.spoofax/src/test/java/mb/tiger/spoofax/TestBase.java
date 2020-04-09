package mb.tiger.spoofax;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceService;
import mb.resource.text.TextResourceRegistry;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.tiger.spoofax.util.DaggerPlatformTestComponent;
import mb.tiger.spoofax.util.DaggerTigerTestComponent;
import mb.tiger.spoofax.util.PlatformTestComponent;
import mb.tiger.spoofax.util.TigerTestComponent;

class TestBase {
    final PlatformTestComponent platformComponent = DaggerPlatformTestComponent
        .builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .build();
    final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    final Logger log = loggerFactory.create(TestBase.class);
    final TextResourceRegistry textResourceRegistry = platformComponent.getTextResourceRegistry();

    final TigerTestComponent languageComponent = DaggerTigerTestComponent
        .builder()
        .platformComponent(platformComponent)
        .tigerModule(new TigerModule())
        .build();
    final ResourceService resourceService = languageComponent.getResourceService();
    final TigerInstance languageInstance = languageComponent.getLanguageInstance();
}
