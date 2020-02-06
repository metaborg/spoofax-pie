package mb.tiger.cli;

import mb.log.slf4j.SLF4JLoggerFactory;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.cli.DaggerSpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCli;
import mb.spoofax.cli.SpoofaxCliComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.tiger.spoofax.DaggerTigerComponent;

public class Main {
    public static void main(String[] args) {
        final SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent
            .builder()
            .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
            .pieModule(new PieModule(PieBuilderImpl::new))
            .build();
        final TigerComponent tigerComponent = DaggerTigerComponent
            .builder()
            .platformComponent(platformComponent)
            .tigerModule(new TigerModule())
            .build();
        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        final int status = cmd.run(args, tigerComponent);
        System.exit(status);
    }
}
