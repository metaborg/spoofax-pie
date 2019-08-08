package mb.tiger.cli;

import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.log.noop.NoopLoggerFactory;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.cli.*;
import mb.spoofax.cli.DaggerSpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCliComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.stratego.common.StrategoRuntimeBuilderException;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSGLR1ParseTableException, StrategoRuntimeBuilderException {
        final SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent
            .builder()
            .loggerFactoryModule(new LoggerFactoryModule(new NoopLoggerFactory()))
            .stringResourceRegistryModule(new StringResourceRegistryModule())
            .pieModule(new PieModule(PieBuilderImpl::new))
            .build();
        final TigerComponent tigerComponent = DaggerTigerComponent
            .builder()
            .platformComponent(platformComponent)
            .tigerModule(TigerModule.fromClassLoaderResources())
            .build();
        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        cmd.run(args, tigerComponent);
        cmd.run(args, tigerComponent);
        cmd.run(args, tigerComponent);
    }
}
