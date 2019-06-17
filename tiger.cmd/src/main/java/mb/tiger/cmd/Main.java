package mb.tiger.cmd;

import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.log.noop.NoopLoggerFactory;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.spoofax.cmd.DaggerSpoofaxCmdComponent;
import mb.spoofax.cmd.SpoofaxCmd;
import mb.spoofax.cmd.SpoofaxCmdComponent;
import mb.spoofax.cmd.StringResourceRegistryModule;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.stratego.common.StrategoRuntimeBuilderException;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSGLR1ParseTableException, StrategoRuntimeBuilderException {
        final SpoofaxCmdComponent platformComponent = DaggerSpoofaxCmdComponent
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
        final SpoofaxCmd cmd = platformComponent.getSpoofaxCmd();
        cmd.run(args, tigerComponent);
        cmd.run(args, tigerComponent);
        cmd.run(args, tigerComponent);
    }
}
