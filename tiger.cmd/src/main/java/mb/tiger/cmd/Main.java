package mb.tiger.cmd;

import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.spoofax.cmd.DaggerSpoofaxCmdComponent;
import mb.spoofax.cmd.SpoofaxCmd;
import mb.spoofax.cmd.SpoofaxCmdComponent;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSGLR1ParseTableException {
        final SpoofaxCmdComponent platformComponent = DaggerSpoofaxCmdComponent.create();
        final TigerComponent tigerComponent = DaggerTigerComponent
            .builder()
            .platformComponent(platformComponent)
            .tigerModule(TigerModule.fromClassLoaderResources())
            .build();
        final SpoofaxCmd cmd = platformComponent.getSpoofaxCmd();
        cmd.run(args, tigerComponent);
    }
}
