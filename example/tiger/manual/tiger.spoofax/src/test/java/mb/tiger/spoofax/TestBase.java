package mb.tiger.spoofax;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceService;
import mb.resource.text.TextResourceRegistry;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;

class TestBase {
    final TextResourceRegistry textResourceRegistry = new TextResourceRegistry();

    final TigerResourcesComponent resourcesComponent = DaggerTigerResourcesComponent.create();
    final BaseResourceServiceModule resourceServiceModule = new BaseResourceServiceModule()
        .addRegistry(textResourceRegistry)
        .addRegistriesFrom(resourcesComponent);
    final BaseResourceServiceComponent resourceServiceComponent = DaggerBaseResourceServiceComponent.builder()
        .baseResourceServiceModule(resourceServiceModule)
        .build();
    final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .resourceServiceComponent(resourceServiceComponent)
        .build();
    final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    final Logger log = loggerFactory.create(TestBase.class);

    final TigerComponent languageComponent = DaggerTigerComponent.builder()
        .tigerModule(new TigerModule())
        .tigerResourcesComponent(resourcesComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .platformComponent(platformComponent)
        .build();
    final ResourceService resourceService = resourceServiceComponent.getResourceService();
    final TigerInstance languageInstance = languageComponent.getLanguageInstance();
}
