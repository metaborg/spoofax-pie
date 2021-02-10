package mb.tiger.cli;

import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.cli.DaggerSpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCli;
import mb.spoofax.cli.SpoofaxCliComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.DaggerTigerResourcesComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerResourcesComponent;

public class Main {
    public static void main(String[] args) {
        final TigerResourcesComponent resourcesComponent = DaggerTigerResourcesComponent.create();
        final BaseResourceServiceModule resourceServiceModule = new BaseResourceServiceModule()
            .addRegistriesFrom(resourcesComponent);
        final ResourceServiceComponent resourceServiceComponent = DaggerBaseResourceServiceComponent.builder()
            .baseResourceServiceModule(resourceServiceModule)
            .build();
        final SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        final TigerComponent component = DaggerTigerComponent.builder()
            .tigerResourcesComponent(resourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        final int status = cmd.run(args, component);
        System.exit(status);
    }
}
