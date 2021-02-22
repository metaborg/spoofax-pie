package mb.tiger.cli;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.spoofax.cli.DaggerSpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCli;
import mb.spoofax.cli.SpoofaxCliComponent;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.DaggerTigerResourcesComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerResourcesComponent;

public class Main {
    public static void main(String[] args) {
        final LoggerComponent loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdErrErrorsAndWarnings())
            .build();
        final TigerResourcesComponent resourcesComponent = DaggerTigerResourcesComponent.create();
        final RootResourceServiceModule resourceServiceModule = new RootResourceServiceModule()
            .addRegistriesFrom(resourcesComponent);
        final ResourceServiceComponent resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(resourceServiceModule)
            .loggerComponent(loggerComponent)
            .build();
        final SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        final TigerComponent component = DaggerTigerComponent.builder()
            .loggerComponent(loggerComponent)
            .tigerResourcesComponent(resourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
        final RootPieComponent pieComponent = DaggerRootPieComponent.builder()
            .rootPieModule(new RootPieModule(PieBuilderImpl::new, component))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        final int status = cmd.run(args, component, pieComponent);
        System.exit(status);
    }
}
